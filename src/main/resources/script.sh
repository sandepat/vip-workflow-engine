# Extract filename without extension
filename=$(basename "${0%.sh}")

# Create the directories if they don't already exist
mkdir -p inv
mkdir -p config

# Copy the files to their respective directories
cp "${filename}-configuration.sh" config/
cp "${filename}-invocation.json" inv/

echo "Files copied successfully."

# Path to the configuration JSON file
configurationFile="config/$filename-configuration.sh"

# Source the configuration file
if [ -f "$configurationFile" ]; then
    source "$configurationFile"
else
    echo "Configuration file $configurationFile not found!"
    exit 1
fi

function info {
  local D=`date`
  echo [ INFO - $D ] $*
}

function warning {
  local D=`date`
  echo [ WARN - $D ] $*
}

function error {
  local D=`date`
  echo [ ERROR - $D ] $* >&2
}

function startLog {
  echo "<$*>" >&1
  echo "<$*>" >&2
}

function stopLog {
  local logName=$1
  echo "</${logName}>" >&1
  echo "</${logName}>" >&2
}

function download_udocker {
  #installation of udocker
  info "cloning udocker ${UDOCKER_TAG} "
  git clone --depth=1 --branch ${UDOCKER_TAG} https://github.com/indigo-dc/udocker.git
  (cd udocker/udocker; ln -s maincmd.py udocker)
  export PATH=`pwd`/udocker/udocker:$PATH
  
  #creating a temporary directory for udocker containers
  mkdir -p containers
  export UDOCKER_CONTAINERS=$PWD/containers
  
  #find pre-deployed containers on CVMFS, and create a symlink to the udocker containers directory
  ## use a global velocity escape to avoid velocity escaping issue
  for d in ${CONTAINERS_CVMFS_PATH}/*/ ;
     do mkdir containers/$(basename "${d%/}") && ln -s "${d%/}"/* containers/$(basename "${d%/}")/
  done
  cat >docker <<'EOF'
        #!/bin/bash
        MYARGS=$*
        echo "executing ./udocker/udocker/udocker $MYARGS"
        ./udocker/udocker/udocker $MYARGS
EOF
  chmod a+x docker
  export PATH=$PWD:$PATH
}

function cleanup {
    if [[ $isGfalmountExec -eq 0 ]]    #flag checks if directories are mounted with gfal
    then
        unmountGfal    #unmounts all gfal mounted directories
        unlink /tmp/*_$(basename $PWD)
    fi
    startLog cleanup
    info "=== ls -a ==="
    ls -a
    info "=== ls $cacheDir/$cacheFile ==="
    ls $cacheDir/$cacheFile
    info "=== cat $cacheDir/$cacheFile === "
    cat $cacheDir/$cacheFile
    info "Cleaning up: rm * -Rf"
    #\rm * -Rf
    if [ "${BACKPID}" != "" ]
    then
        for i in `ps --ppid ${BACKPID} -o pid | grep -v PID`
        do
            info "Killing child of background script (pid ${i})"
            kill -9 ${i}
        done
        info "Killing background script (pid ${BACKPID})"
        kill -9 ${BACKPID}
    fi
    info "END date:"
    date +%s
    stopLog cleanup
    check_cleanup=true
}

export -f cleanup
trap 'echo "trap activation" && stopRefreshingToken | \
if [ "$check_cleanup" = true ]; then \
    echo "cleanup was already executed successfully"; \
else \
    echo "Executing cleanup" && cleanup; \
fi' INT EXIT


function checkCacheDownloadAndCacheLFN {

    local LFN=$1
    local download="true"

    local LOCALPATH=$(awk -v L=${LFN} '$1==L {print $2}' $cacheDir/$cacheFile)
    if [ -n "${LOCALPATH}" ]; then
        info "There is an entry in the cache: test if the local file still here"
        local TIMESTAMP_LOCAL=""
        local TIMESTAMP_GRID=""
        local date_local=""
        if [ -f "${LOCALPATH}" ]; then
            info "The file exists: checking if it was modified since it was added to the cache"
            local YEAR=$(date +%Y)
            local YEARBEFORE=$(expr ${YEAR} - 1)
            local currentDate=$(date +%s)
            local TIMESTAMP_CACHE=$(awk -v L=${LFN} '$1==L {print $3}' $cacheDir/$cacheFile)
            local LOCALMONTH=$(ls -la ${LOCALPATH} | awk -F' ' '{print $6}')
            local MONTHTIME=$(date -d "${LOCALMONTH} 1 00:00" +%s)
            date_local=$(ls -la ${LOCALPATH} | awk -F' ' '{print $6, $7, $8}')
            if [ "${MONTHTIME}" -gt "${currentDate}" ]; then
                TIMESTAMP_LOCAL=$(date -d "${date_local} ${YEARBEFORE}" +%s)
            else
                TIMESTAMP_LOCAL=$(date -d "${date_local} ${YEAR}" +%s)
            fi
            if [ "${TIMESTAMP_CACHE}" = "${TIMESTAMP_LOCAL}" ]; then
                info "The file was not touched since it was added to the cache: test if it is up-to-date"
                local date_grid_s=$(lfc-ls -l ${LFN} | awk -F' ' '{print $6, $7, $8}')
                local MONTHGRID=$(echo ${date_grid_s} | awk -F' ' '{print $1}')
                MONTHTIME=$(date -d "${MONTHGRID} 1 00:00" +%s)
                if [ -n "${MONTHTIME}" ] && [ -n "${date_grid_s}" ]; then
                    if [ "${MONTHTIME}" -gt "${currentDate}" ]; then
                        # it must be last year
                        TIMESTAMP_GRID=$(date -d "${date_grid_s} ${YEARBEFORE}" +%s)
                    else
                        TIMESTAMP_GRID=$(date -d "${date_grid_s} ${YEAR}" +%s)
                    fi
                    if [ "${TIMESTAMP_LOCAL}" -gt "${TIMESTAMP_GRID}" ]; then
                        info "The file is up-to-date ; there is no need to download it again"
                        download="false"
                    else
                        warning "The cache entry is outdated (local modification date is ${TIMESTAMP_LOCAL} - ${date_local} while grid is ${TIMESTAMP_GRID} ${date_grid_s})"
                    fi
                else
                    warning "Cannot determine file timestamp on the LFC"
                fi
            else
                warning "The cache entry was modified since it was created (cache time is ${TIMESTAMP_CACHE} and file time is ${TIMESTAMP_LOCAL} - ${date_local})"
            fi
        else
            warning "The cache entry disappeared"
        fi
    else
        info "There is no entry in the cache"
    fi
    
    if [ "${download}" = "false" ]; then
        info "Linking file from cache: ${LOCALPATH}"
        BASE=$(basename ${LFN})
        echo "BASE : ${BASE}"
        echo "LOCALPATH : ${LOCALPATH}"
        info "ln -s ${LOCALPATH} ./${BASE}"
        ln -s ${LOCALPATH} ./${BASE}
        return 0
    fi

    if [ "${download}" = "true" ]; then
        echo "${LFN}"
        downloadLFN ${LFN}
        if [ $? != 0 ]; then
            return 1
        fi
        addToCache ${LFN} $(basename ${LFN})
        return 0
    fi
}

export -f checkCacheDownloadAndCacheLFN


SHANOIR_TOKEN_LOCATION="${PWD}/cache/SHANOIR_TOKEN.txt"
SHANOIR_REFRESH_TOKEN_LOCATION="${PWD}/cache/SHANOIR_REFRESH_TOKEN.txt"
REFRESHING_JOB_STARTED=false
REFRESH_PID=""

#
# This is a background process to refresh shanoir token
# URIs are of the form of the following example. A single "/", instead
# of 3, after "shanoir:" is also allowed.
# shanoir:/path/to/file/filename?&refreshToken=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lk....&keycloak_client_id=....&keycloak_client_secret=...
# The mandatory arguments are: keycloak_client_id, keycloak_client_secret.
#
function refresh_token {
    touch "$SHANOIR_TOKEN_LOCATION"
    touch "$SHANOIR_REFRESH_TOKEN_LOCATION"

    local subshell_refresh_token=$(cat "$SHANOIR_REFRESH_TOKEN_LOCATION")

    echo "refresh token process started !"

    local URI=$1
    local keycloak_client_id=$(echo "$URI" | sed -r 's/^.*[?&]keycloak_client_id=([^&]*)(&.*)?$/\1/i')
    local refresh_token_url=$(echo "$URI" | sed -r 's/^.*[?&]refresh_token_url=([^&]*)(&.*)?$/\1/i')

    if [[ ! "$subshell_refresh_token" ]]; then
        # initializing the refresh token
        subshell_refresh_token=$(echo "$URI" | sed -r 's/^.*[?&]refreshToken=([^&]*)(&.*)?$/\1/i')
        echo "$subshell_refresh_token" > "$SHANOIR_REFRESH_TOKEN_LOCATION"
    fi

    while :; do

        # get the new refresh token
        subshell_refresh_token=$(cat "$SHANOIR_REFRESH_TOKEN_LOCATION")

        # the response format is "{"status":"status"}"
        # this response format is made to handle error while getting the refreshed token in the same time
        COMMAND() {
            curl -w "{\"status\":\"%{http_code}\"}" -sb -o --request POST "${refresh_token_url}" --header "Content-Type: application/x-www-form-urlencoded" --data-urlencode "client_id=${keycloak_client_id}" --data-urlencode "grant_type=refresh_token" --data-urlencode "refresh_token=${subshell_refresh_token}"
        }

        refresh_response=$(COMMAND)
        status_code=$(echo "$refresh_response" | grep -o '"status":"[^"]*' | grep -o '[^"]*$')

        if [[ "$status_code" -ne 200 ]]; then
            error_message=$(echo "$refresh_response" | grep -o '"error_description":"[^"]*' | grep -o '[^"]*$')
            error "error while refreshing the token with status : ${status_code} and message error : ${error_message}"
            exit 1
        fi

        # setting the new tokens
        echo "$refresh_response" | grep -o '"access_token":"[^"]*' | grep -o '[^"]*$' > "$SHANOIR_TOKEN_LOCATION"
        echo "$refresh_response" | grep -o '"refresh_token":"[^"]*' | grep -o '[^"]*$' > "$SHANOIR_REFRESH_TOKEN_LOCATION"

        sleep 240
    done

}
#
# Cleanup method: stop the refreshing process
#

function stopRefreshingToken {
    if [ "${REFRESH_PID}" != "" ]; then
        info "Killing background refresh token process with id : ${REFRESH_PID}"
        kill -9 "${REFRESH_PID}"
        REFRESH_PID=""
        echo "refresh token process ended !"
    fi
}
#
# The refresh token may take some time, this method is for that purpose
# and it exit the program if it's timed out
#

function wait_for_token {
    local token=""
    local attempts=0

    while [[ "${attempts}" -ne 3 ]]; do
        token=$(cat "$SHANOIR_TOKEN_LOCATION")
        if [[ "${token}" == "" ]]; then
            echo "token is not refreshed yet, waiting for 3 seconds..."
            echo "attempts : ${attempts}"
            attempts=$((attempts + 1))
            sleep 3
        else
            echo "token is refreshed !"
            break
        fi
    done

    # Check the token after the timeout
    if [[ -z "${token}" ]]; then
        echo "Token refreshing is taking too long. Aborting the process."
        stopRefreshingToken
        exit 1
    fi
}


function downloadLFN {

    local LFN=$1
    echo "LFN : ${LFN}"

    # Sanitize LFN:
    # - "lfn:" at the beginning is optional for dirac-dms-* commands,
    #    but does not work as expected with comdirac commands like
    #    dmkdir.
    # - "//" are not accepted, neither by dirac-dms-*, nor by dmkdir.
    LFN=$(echo ${LFN} | sed -r -e 's/^lfn://' -e 's#//#/#g')

    info "getting file size and computing sendReceiveTimeout"
    local size=$(dirac-dms-lfn-metadata ${LFN} | grep Size | sed -r 's/.* ([0-9]+)L,/\1/')
    #local sendReceiveTimeout=`echo ${D}[${D}{size:-0}/${minAvgDownloadThroughput}/1024]`
    #############################
        # Compute sendReceiveTimeout
    if [ -z "$size" ]; then
        size=0
    fi

    local sendReceiveTimeout=$((size / minAvgDownloadThroughput / 1024))
    ############################
    
    if [ "$sendReceiveTimeout" = "" ] || [ $sendReceiveTimeout -le 900 ]
    then
        info "sendReceiveTimeout empty or too small, setting it to 900s"
        sendReceiveTimeout=900
    else
        info "sendReceiveTimeout is $sendReceiveTimeout"
    fi

    local LOCAL=${PWD}/`basename ${LFN}`
    info "Removing file ${LOCAL} in case it is already here"
    \rm -f ${LOCAL}

    local totalTimeout=$((${timeout} + ${srmTimeout} + ${sendReceiveTimeout}))

    local LINE="time -p dirac-dms-get-file -d -o /Resources/StorageElements/GFAL_TIMEOUT=${totalTimeout} ${LFN}"
    info ${LINE}
    (${LINE}) &> get-file.log

    if [ $? = 0 ]
    then
        info "dirac-dms-get-file worked fine"
        local source=$(grep "generating url" get-file.log | tail -1 | sed -r 's/^.* (.*)\.$/\1/')
        local duration=$(grep -P '^real[ \t]' get-file.log | sed -r 's/real[ \t]//')
        info "DownloadCommand=dirac-dms-get-file Source=${source} Destination=$(hostname) Size=${size} Time=${duration}"
        RET_VAL=0
    else
        error "dirac-dms-get-file failed"
        error "`cat get-file.log`"
        RET_VAL=1
    fi

    \rm get-file.log
    return ${RET_VAL}
}
export -f downloadLFN
#
# URI are of the form of the following example. A single "/", instead
# of 3, after "girder:" is also allowed.
# girder:///control_3DT1.nii?apiurl=http://localhost:8080/api/v1&fileId=5ae1a8fc371210092e0d2936&token=TFT2FdxP9hzM7WKsidBjMJMmN69
#
#
# The code is quite the same as the uploadGirderFile function.  Any
# changes should be done the same in both functions.
#
function downloadGirderFile {
    local URI=$1

    # The regexpes are written so that case is ignored and the
    # arguments can be in any order.
    local fileName=$(echo $URI | sed -r 's#^girder:/(//)?([^/].*)\?.*$#\2#i')
    local apiUrl=$(echo $URI | sed -r 's/^.*[?&]apiurl=([^&]*)(&.*)?$/\1/i')
    local fileId=$(echo $URI | sed -r 's/^.*[?&]fileid=([^&]*)(&.*)?$/\1/i')
    local token=$(echo $URI | sed -r 's/^.*[?&]token=([^&]*)(&.*)?$/\1/i')

    if [ ! $(which girder-client) ]; then
        pip install --user girder-client
        if [ $? != 0 ]; then
            echo "girder-client not in PATH, and an error occurred while trying to install it."
            echo "Exiting with return value 1"
            exit 1
        fi
    fi

    COMMLINE="girder-client --api-url ${apiUrl} --token ${token} download --parent-type file ${fileId} ./${fileName}"
    echo "downloadGirderFile, command line is ${COMMLINE}"
    ${COMMLINE}
}

export -f downloadGirderFile


# This function identifies the gfal path and extracts the basename of the directory to be mounted,
# and creates a directory with the exact name on $PWD of the node. This directory gets mounted with
# the corresponding directory on the SE.
#
# This function checks for all the gfal mounts in the current folder.

check_mount='$(test -z $(for file in *; do findmnt -t fuse.gfalFS -lo Target -n -T $(realpath ${file}); done) && echo 1 || echo 0)'
isGfalmountExec=1

function mountGfal {
    local URI=$1

    # The regexpes are written so that case is ignored and the
    # arguments can be in any order.
    local fileName=$(echo $URI | sed -r 's#^srm:/(//)?([^/].*)\?.*$#\2#i')
    local gfal_basename=$(basename ${fileName})
    local job_id=${gfal_basename}_$(basename $PWD)

    CREATE_DIR_COMMAND="mkdir -p $gfal_basename"
    SYM_LINK_COMMAND="ln -s $PWD/$gfal_basename /tmp/$job_id"
    GFAL_COMMAND="gfalFS -s /tmp/$job_id ${fileName}"

    ${CREATE_DIR_COMMAND}
    ${SYM_LINK_COMMAND}
    ${GFAL_COMMAND}
    # Let nfs-kernel-server export the directory and write logs
    sleep 30
    eval echo $check_mount
}

export -f mountGfal


# This function un-mounts all the gfal mounted directories by searching them with 'findmnt'
# and filtering them with FSTYPE 'fuse.gfalFS'. This function gets called in the cleanup function,
# either after the execution of the job, failure of the job, or interruptions of the job.
function unmountGfal {
    START=$SECONDS
    while [ $(eval echo $check_mount) = 0 ]; do
        for file in $PWD/*; do
            findmnt -t fuse.gfalFS -lo Target -n -T $(realpath ${file}) && gfalFS_umount $(realpath ${file})
        done
        sleep 2
        if [[ $(($SECONDS - $START)) -gt 600 ]]; then # while loop breaks automatically after 10 minutes
            echo "WARNING - gfal directory couldn't be unmounted: timeout"
            break
        fi

    done
    eval echo $check_mount
}

export -f unmountGfal


# URI are of the form of the following example. A single "/", instead
# of 3, after "shanoir:" is also allowed.
# shanoir:/download.dcm?apiurl=https://shanoir-ng-nginx/shanoir-ng/datasets/carmin-data/path&format=dcm&datasetId=1
#
# This method depends on the refresh token process to refresh the token when it needs
#
function downloadShanoirFile {
    local URI=$1
    
    wait_for_token

    local token=`cat $SHANOIR_TOKEN_LOCATION`

    echo "token inside download : ${token}"

    local fileName=`echo $URI | sed -r 's#^shanoir:/(//)?([^/].*)\?.*$#\2#i'`
    local apiUrl=`echo $URI | sed -r 's/^.*[?&]apiurl=([^&]*)(&.*)?$/\1/i'`
    local format=`echo $URI | sed -r 's/^.*[?&]format=([^&]*)(&.*)?$/\1/i'`
    local resourceId=`echo $URI | sed -r 's/^.*[?&]resourceId=([^&]*)(&.*)?$/\1/i'`

    COMMAND(){
        curl --write-out '%{http_code}' -o ${fileName} --request GET "${apiUrl}/${resourceId}?format=${format}" --header "Authorization: Bearer ${token}"
    }

    local attempts=0

    while [[ "${attempts}" -ne 3 ]]; do
        status_code=$(COMMAND)
        info "downloadShanoirFIle, status code is : ${status_code}"

        if [[ "$status_code" -ne 200 ]]; then
            error "error while downloading the file with status : ${status_code}"
            attempts=$((attempts + 1))
            info "${attempts} done. Waiting 3 seconds and maybe do another attempt"
            sleep 3
        else
            break
        fi
    done

    if [[ "${attempts}" -ge 3 ]]; then
        error "3 failures at downloading, stop trying and stop the job"
        stopRefreshingToken
        exit 1
    fi

    if [[ $format = "nii" ]]; then
       echo "its a nifti, shanoir has zipped it"
       TMP_UNZIP_DIR="tmp_unzip_dir"
       mkdir $TMP_UNZIP_DIR
       mv $fileName $TMP_UNZIP_DIR/tmp.zip
       unzip -d $TMP_UNZIP_DIR $TMP_UNZIP_DIR/tmp.zip
       # there should be a unique .nii ou .nii.gz file somewhere
       searchResult=$(find $TMP_UNZIP_DIR -name '*.nii.gz' -o -name '*.nii')
       # doing this trick instead of using "wc -l" because it fails when there is no result
       if [[ $(echo -n "$searchResult" | grep -c '^') -ne 1 ]]; then
            error "too many or none nifti file (.nii or .nii.gz) in shanoir zip, supporting only 1"
            stopRefreshingToken
            exit 1
       fi
       mv "$searchResult" "$fileName"
       rm -rf $TMP_UNZIP_DIR
    fi
}

function downloadURI {

    local URI=$1
    local URI_LOWER=`echo $1 | awk '{print tolower($0)}'`


    if [[ ${URI_LOWER} == lfn* ]] || [[ $URI_LOWER == /* ]]
    then
        ## Extract the path part from the uri, and remove // if
        ## present in path.
        LFN=`echo "${URI}" | sed -r -e 's%^\w+://[^/]*(/[^?]+)(\?.*)?$%\1%' -e 's#//#/#g'`

        checkCacheDownloadAndCacheLFN $LFN
        validateDownload "Cannot download LFN file"
    fi

    if [[ ${URI_LOWER} == file:/* ]]
    then
        local FILENAME=`echo $URI | sed 's%file://*%/%'`
        cp $FILENAME .
        validateDownload "Cannot copy input file: $FILENAME"
    fi

    if [[ ${URI_LOWER} == http://* ]]
    then
        curl --insecure -O ${URI}
        validateDownload "Cannot download HTTP file"
    fi

    if [[ ${URI_LOWER} == girder:/* ]]
    then
        downloadGirderFile ${URI}
        validateDownload "Cannot download Girder file"
    fi

    if [[ ${URI_LOWER} == shanoir:/* ]]
    then
        if [[ "$REFRESHING_JOB_STARTED" == false ]]; then
            refresh_token ${URI} & 
            REFRESH_PID=$!  
            REFRESHING_JOB_STARTED=true
        fi
        downloadShanoirFile ${URI}
        validateDownload "Cannot download shanoir file"
    fi

    if [[ ${URI_LOWER} == srm:/* ]] 
    then
            if [[ $(mountGfal ${URI}) -eq 0 ]]
                then
                    isGfalmountExec=0
                else
                    echo "Cannot download gfal file"
            fi
    fi
}
function validateDownload() {

    if [ $? != 0 ]; then
        echo "$1"
        echo "Exiting with return value 1"
        exit 1
    fi
}


function addToCache {
    cacheDir=${BASEDIR}/cache
    mkdir -p "$cacheDir"
    touch "$cacheDir/$cacheFile"
    local LFN="$1"
    local FILE=$(basename "$2")
    local i=0
    local exist="true"
    local NAME=""
    while [ "$exist" = "true" ]; do
        NAME="$cacheDir/${FILE}-cache-${i}"
        test -f "${NAME}"
        if [ $? != 0 ]; then
            exist="false"
        fi
        i=$(expr $i + 1)
    done
    info "Removing all cache entries for ${LFN} (files will stay locally in case anyone else needs them)"
    local TEMP=$(mktemp temp.XXXXXX)
    awk -v L="${LFN}" '$1!=L {print}' "$cacheDir/$cacheFile" > "${TEMP}"
    \mv -f "${TEMP}" "$cacheDir/$cacheFile"
    info "Adding file ${FILE} to cache and setting the timestamp"
    \cp -f "${FILE}" "${NAME}"
    local date_local=$(ls -la "${NAME}" | awk -F' ' '{print $6, $7, $8}')
    local TIMESTAMP=$(date -d "${date_local}" +%s)
    echo "${LFN} ${NAME} ${TIMESTAMP}" >> "$cacheDir/$cacheFile"
}

export -f addToCache


nSEs() {
    local i=0
    for n in ${SELIST}; do
        i=$((i + 1))
    done
    return $i
}

getAndRemoveSE() {
    local index=$1
    local i=0
    local NSE=""
    RESULT=""
    for n in ${SELIST}; do
        if [ "$i" = "${index}" ]; then
            RESULT=$n
            info "result: $RESULT"
        else
            NSE="${NSE} $n"
        fi
        i=$((i + 1))
    done
    SELIST=${NSE}
    return 0
}

chooseRandomSE() {
    nSEs
    local n=$?
    if [ "$n" = "0" ]; then
        info "SE list is empty"
        RESULT=""
    else
        local r=${RANDOM}
        local id=$((r % n))
        getAndRemoveSE ${id}
    fi
}

uploadLfnFile() {
    local LFN=$1
    local FILE=$2
    local nrep=$3
    local SELIST=${SE}

    # Sanitize LFN:
    # - "lfn:" at the beginning is optional for dirac-dms-* commands,
    #    but does not work as expected with comdirac commands like
    #    dmkdir.
    # - "//" are not accepted, neither by dirac-dms-*, nor by dmkdir.
    LFN=$(echo ${LFN} | sed -r -e 's/^lfn://' -e 's#//#/#g')

    info "getting file size and computing sendReceiveTimeout"
    local size=$(ls -l ${FILE} | awk -F' ' '{print $5}')
    local sendReceiveTimeout=$(((${size:-0} / minAvgDownloadThroughput / 1024)))
    if [ -z "$sendReceiveTimeout" ] || [ "$sendReceiveTimeout" -le 900 ]; then
        info "sendReceiveTimeout empty or too small, setting it to 900s"
        sendReceiveTimeout=900
    else
        info "sendReceiveTimeout is $sendReceiveTimeout"
    fi

    local totalTimeout=$((timeout + srmTimeout + sendReceiveTimeout))

    local OPTS="-o /Resources/StorageElements/GFAL_TIMEOUT=${totalTimeout}"
    chooseRandomSE
    local DEST=${RESULT}
    local done=0
    while [ $nrep -gt $done ] && [ "${DEST}" != "" ]; do
        if [ "${done}" = "0" ]; then
            local command="dirac-dms-add-file"
            local source=$(hostname)
            dirac-dms-remove-files ${OPTS} ${LFN} &>/dev/null
            (time -p dirac-dms-add-file ${OPTS} ${LFN} ${FILE} ${DEST}) &> dirac.log
            local error_code=$?
        else
            local command="dirac-dms-replicate-lfn"
            (time -p dirac-dms-replicate-lfn -d ${OPTS} ${LFN} ${DEST}) &> dirac.log
            local error_code=$?

            local source=$(grep "operation 'getFileSize'" dirac.log | tail -1 | sed -r 's/^.* StorageElement (.*) is .*$/\1/')
        fi
        if [ ${error_code} = 0 ]; then
            info "Copy/Replication of ${LFN} to SE ${DEST} worked fine."
            done=$((done + 1))
            local duration=$(grep -P '^real[ \t]' dirac.log | sed -r 's/real[ \t]//')
            info "UploadCommand=${command} Source=${source} Destination=${DEST} Size=${size} Time=${duration}"
            if [ -z "${duration}" ]; then
                info "Missing duration info, printing the whole log file."
                cat dirac.log
            fi
        else
            error "$(cat dirac.log)"
            warning "Copy/Replication of ${LFN} to SE ${DEST} failed"
        fi
        \rm dirac.log
        chooseRandomSE
        DEST=${RESULT}
    done
    if [ "${done}" = "0" ]; then
        error "Cannot copy file ${FILE} to lfn ${LFN}"
        error "Exiting with return value 2"
        exit 2
    else
        addToCache ${LFN} ${FILE}
    fi
}

#
# This method is used to upload results of an execution to an upload url.
# URI are of the form of the following example.  A single "/", instead
# of 3, after "shanoir:" is also allowed.
# shanoir:/path/to/file/filename?upload_url=https://upload/url/&type=File&md5=None
# 
# This method depends on refresh token process to refresh the token when it needs
#

uploadShanoirFile() {
    local URI=$1

    wait_for_token

    local token=$(cat $SHANOIR_TOKEN_LOCATION)

    local upload_url=$(echo $URI | sed -r 's/^.*[?&]upload_url=([^&]*)(&.*)?$/\1/i')
    local fileName=$(echo $URI | sed -r 's#^shanoir:/(//)?(.*/(.+))\?.*$#\3#i')
    local filePath=$(echo $URI | sed -r 's#^shanoir:/(//)?([^/].*)\?.*$#\2#i')
   
    local type=$(echo $URI | sed -r 's/^.*[?&]type=([^&]*)(&.*)?$/\1/i')
    local md5=$(echo $URI | sed -r 's/^.*[?&]md5=([^&]*)(&.*)?$/\1/i')

    COMMAND() { 
        (echo -n '{"base64Content": "'; base64 ${fileName}; echo '", "type":"'; echo ${type}; echo '", "md5":"'; echo ${md5} ; echo '"}') | curl --write-out '%{http_code}' --request PUT "${upload_url}/${filePath}"  --header "Authorization: Bearer ${token}"  --header "Content-Type: application/carmin+json" --header 'Accept: application/json, text/plain, */*' -d @-
    }

    status_code=$(COMMAND)
    echo "uploadShanoirFIle, status code is : ${status_code}"
    
    if [[ "$status_code" -ne 201 ]]; then
        error "error while uploading the file with status : ${status_code}"
        stopRefreshingToken
        exit 1
    fi
}

#
# URI are of the form of the following example.  A single "/", instead
# of 3, after "girder:" is also allowed.
# girder:///control_3DT1.nii?apiurl=http://localhost:8080/api/v1&fileId=5ae1a8fc371210092e0d2936&token=TFT2FdxP9hzM7WKsidBjMJMmN69
#
# The code is quite the same as the downloadGirderFile function.  Any
# changes should be done the same in both functions.
#

uploadGirderFile() {
    local URI=$1

    local fileName=$(echo $URI | sed -r 's#^girder:/(//)?(.*/)?([^/].*)\?.*$#\3#i')
    local apiUrl=$(echo $URI | sed -r 's/^.*[?&]apiurl=([^&]*)(&.*)?$/\1/i')
    local fileId=$(echo $URI | sed -r 's/^.*[?&]fileid=([^&]*)(&.*)?$/\1/i')
    local token=$(echo $URI | sed -r 's/^.*[?&]token=([^&]*)(&.*)?$/\1/i')

    if [ ! $(which girder-client) ]; then
        pip install --user girder-client
        if [ $? != 0 ]; then
            error "girder-client not in PATH, and an error occured while trying to install it."
            error "Exiting with return value 1"
            exit 1
        fi
    fi

    COMMLINE="girder-client --api-url ${apiUrl} --token ${token} upload --parent-type folder ${fileId} ./${fileName}"
    echo "uploadGirderFile, command line is ${COMMLINE}"
    ${COMMLINE}
    if [ $? != 0 ]; then
        error "Error while uploading girder file"
        error "Exiting with return value 1"
        exit 1
    fi
}

function upload {
    local URI=$1
    local ID=$2
    local NREP=$3
    local TEST=$4

    startLog file_upload uri="${URI}"
    
    # The pattern must NOT be put between quotation marks.
    if [[ ${URI} == shanoir:/* ]]; then
        if [ "${TEST}" != "true" ]; then
            if [ "$REFRESHING_JOB_STARTED" == false ]; then
                refresh_token ${URI} &
                REFRESH_PID=$!  
                REFRESHING_JOB_STARTED=true
            fi
            uploadShanoirFile ${URI}
        fi
    elif [[ ${URI} == girder:/* ]]; then
        if [ "${TEST}" != "true" ]; then
            uploadGirderFile ${URI}
        fi
    elif [[ ${URI} == file:/* ]]; then
        local FILENAME=$(echo $URI | sed 's%file://*%/%')
        local NAME=$(basename ${FILENAME})

        if [ -e $FILENAME ]; then
            error "Result file already exists: $FILENAME"
            error "Exiting with return value 1"
            exit 1
        fi

        if [ "${TEST}" = "true" ]; then
            echo "test result" > ${NAME}
        fi

        mv $NAME $FILENAME
        if [ $? != 0 ]; then
            error "Error while moving result local file."
            error "Exiting with return value 1"
            exit 1
        fi
    else
        # Extract the path part from the uri.
        local LFN=$(echo "${URI}" | sed -r 's%^\w+://[^/]*(/[^?]+)(\?.*)?$%\1%')
        local NAME=${LFN##*/}

        if [ "${TEST}" = "true" ]; then
            LFN=${LFN}-uploadTest
            echo "test result" > ${NAME}
        fi

        uploadLfnFile ${LFN} ${PWD}/${NAME} ${NREP}

        if [ "${TEST}" = "true" ]; then
            rm -f ${NAME}
        fi
    fi

    stopLog file_upload
}


function delete {

    local URI="$1"
    local TEST="$2"

    startLog file_delete uri="${URI}"

    ## The pattern must NOT be put between quotation marks.
    if [[ ${URI} == girder:/* ]]; then
        info "delete not supported for girder"
    elif [[ ${URI} == file:/* ]]; then
        local FILENAME=$(echo "$URI" | sed 's%file://*%/%')

        info "Removing local file ${FILENAME}..."
        \rm -f "$FILENAME"
    else
        ## Extract the path part from the uri, and sanitize it.
        ## "//" are not accepted by dirac commands.
        local LFN=$(echo "${URI}" | sed -r -e 's%^\w+://[^/]*(/[^?]+)(\?.*)?$%\1%' -e 's#//#/#g')

        if [ "${TEST}" = true ]; then
            LFN="${LFN}-uploadTest"
        fi

        info "Deleting file ${LFN}..."
        dirac-dms-remove-files "${LFN}"
    fi

    stopLog file_delete
}


####################################################################################################
####################################################################################################
function checkBosh {
  local BOSH_CVMFS_PATH=$1
  #by default, use CVMFS bosh
  ${BOSH_CVMFS_PATH}/bosh create foo.sh
  if [ $? != 0 ]
  then
    info "CVMFS bosh in ${BOSH_CVMFS_PATH} not working, checking for a local version"
    bosh create foo.sh
    if [ $? != 0 ]
    then
        info "bosh is not found in PATH or it is does not work fine, searching for another local version"
        local HOMEBOSH=`find $HOME -name bosh`
        if [ -z "$HOMEBOSH" ]
        then
            info "bosh not found, trying to install it"
            pip install --trusted-host pypi.org --trusted-host pypi.python.org --trusted-host files.pythonhosted.org boutiques --prefix $PWD 
            if [ $? != 0 ]
            then
                error "pip install boutiques failed"
                exit 1
            else
                export BOSHEXEC="$PWD/bin/bosh"
            fi
        else
            info "local bosh found in $HOMEBOSH"
            export BOSHEXEC=$HOMEBOSH
        fi
    else # bosh is found in PATH and works fine
        info "local bosh found in $PATH"
        export BOSHEXEC="bosh"
    fi
  else # if bosh CVMFS works fine
    export BOSHEXEC="${BOSH_CVMFS_PATH}/bosh"
  fi
}

function copyProvenanceFile() {
  local dest=$1
  # $BOUTIQUES_PROV_DIR is defined by GASW from the settings file
  if [ ! -d "$BOUTIQUES_PROV_DIR" ]; then
    error "Boutiques cache dir $BOUTIQUES_PROV_DIR does not exist."
    return 1
  fi
  local provenanceFile=$(ls -t "$BOUTIQUES_PROV_DIR" | grep -v "^descriptor_" | head -n 1)
  if [[ -z "$provenanceFile" ]]; then
    error "No provenance found in boutiques cache $BOUTIQUES_PROV_DIR"
    return 2
  fi
  info "Found provenance file $BOUTIQUES_PROV_DIR/$provenanceFile"
  info "Copying it to $dest"
  cp $BOUTIQUES_PROV_DIR/$provenanceFile $BASEDIR
  cp $BOUTIQUES_PROV_DIR/$provenanceFile $dest
}

startLog header
# Start log
START=$(date +%s)
echo "START date is ${START}"

# Execution environment setup
export GASW_JOB_ENV=NORMAL
export GASW_EXEC_ENV=EGEE

# Builds the custom environment
export BASEDIR=${PWD}
ENV=$defaultEnvironment
export $ENV
export SE=$voDefaultSE
USE_CLOSE_SE=$voUseCloseSE
export BOSH_CVMFS_PATH=$boshCVMFSPath
export CONTAINERS_CVMFS_PATH=$containersCVMFSPath
export UDOCKER_TAG=$udockerTag
export BOUTIQUES_PROV_DIR=$boutiquesProvenanceDir

export MOTEUR_WORKFLOWID="$simulationID"

# If the execution environment is a cluster, add the vlet binaries to the path
if [[ "$GASW_EXEC_ENV" == "PBS" ]]; then
    export PATH=${VLET_INSTALL}/bin:$PATH
fi

DIAG=/home/grid/session/$(basename ${PWD}).diag

# Create execution directory
DIRNAME=$(basename $0 .sh)
mkdir ${DIRNAME}
if [ $? -eq 0 ]; then
    echo "cd ${DIRNAME}"
    cd ${DIRNAME} || exit 7
else
    echo "Unable to create directory ${DIRNAME}"
    echo "Exiting with return value 7"
    exit 7
fi

if [[ $minorStatusEnabled == true && $serviceCall ]]; then
    $serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 1
fi

BACKPID=""

# DIRAC may wrongly position this variable
if [ ! -d ${X509_CERT_DIR} ]; then
    echo "Unsetting invalid X509_CERT_DIR (${X509_CERT_DIR})"
    unset X509_CERT_DIR
fi

echo "END date is $(date +%s)"

stopLog header


startLog host_config

echo "SE Linux mode is:"
/usr/sbin/getenforce
echo "gLite Job Id is ${GLITE_WMS_JOBID}"
echo "===== uname ===== "
uname -a
domainname -a
echo "===== network config ===== "
/sbin/ifconfig eth0
dmesg_line=$(dmesg | grep 'Link is Up' | uniq)
netspeed=$(echo $dmesg_line | grep -o '[0-9]*[[:space:]][a-zA-Z]bps'| awk '{gsub(/ /,"",$0);print}')
echo "NetSpeed = $netspeed ($dmesg_line)"
echo "===== CPU info ===== "
cat /proc/cpuinfo
echo "===== Memory info ===== "
cat /proc/meminfo
echo "===== lcg-cp location ===== "
which lcg-cp
echo "===== ls -a . ===== "
ls -a
echo "===== ls -a .. ===== "
ls -a ..
echo "===== env ====="
env
echo "===== rpm -qa  ===="
rpm -qa

mkdir -p $cacheDir

stopLog host_config


startLog background

# Execute service call if minor status is enabled and service call is provided
if [[ "$minorStatusEnabled" == true && -n "$serviceCall" ]]; then
    $serviceCall $MOTEUR_WORKFLOWID $JOBID 2
fi

# Check cache, download, and cache LFN for the background script
checkCacheDownloadAndCacheLFN $backgroundScript

# Execute the background script
bash $(basename $backgroundScript) 1>background.out 2>background.err &
BACKPID=$!

# Stop log
stopLog background


startLog inputs_download


# Execute service call if minor status is enabled and service call is provided
if [[ "$minorStatusEnabled" == true && -n "$serviceCall" ]]; then
    $serviceCall $MOTEUR_WORKFLOWID $JOBID 3
fi

# Create a file to disable watchdog CPU wallclock check
touch ../DISABLE_WATCHDOG_CPU_WALLCLOCK_CHECK

###################################################################################
# Remove square brackets and leading/trailing whitespace from downloads
downloads="${downloads#[}"
downloads="${downloads%]}"
downloads="${downloads// /}"

IFS=',' read -ra download_array <<< "$downloads"

# Iterate over each URL in the 'downloads' array
for download in "${download_array[@]}"; do
    # Remove leading and trailing whitespace
    download="$(echo -e "${download}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
    # Process the URL using downloadURI function
    downloadURI "$download"
    # Print the processed URL
    echo "$download"
done


# Change permissions of all files in the directory
chmod 755 *

# Record the timestamp after downloads
AFTERDOWNLOAD=$(date +%s)

# Stop log for inputs download
stopLog inputs_download


startLog application_environment
echo "$variables"

# Iterate through each variable in $variables and export them
# Assuming $variables is a string containing key-value pairs separated by space
for variable in $variables; do
    key=$(echo "$variable" | cut -d'=' -f1)
    value=$(echo "$variable" | cut -d'=' -f2)
    export "$key"="$value"
done

# Stop log for application environment
stopLog application_environment

startLog application_execution

# Perform service call if minor status is enabled
if [[ $minorStatusEnabled == true && $serviceCall ]]; then
    $serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 4
fi

# Add a delay to ensure file creation before proceeding
echo "BEFORE_EXECUTION_REFERENCE" > BEFORE_EXECUTION_REFERENCE_FILE
sleep 1

checkBosh $BOSH_CVMFS_PATH

####################################################################################################
# Clone udocker (A basic user tool to execute simple docker containers in batch or interactive systems without root privileges)
if ! command -v docker
then
    download_udocker
fi
####################################################################################################

# Export current directory to LD_LIBRARY_PATH
export LD_LIBRARY_PATH=${PWD}:${LD_LIBRARY_PATH}

#####
# Temporary fix
jsonFileName="workflow.json"
######

echo "import sys; sys.setdefaultencoding(\"UTF8\")" > sitecustomize.py #breaking for Python3. Need review
# Execute the command
PYTHONPATH=".:$PYTHONPATH" $BOSHEXEC exec launch $jsonFileName ../inv/$invocationJson -v $PWD/../cache:$PWD/../cache

# Check if execution was successful
if [ $? -ne 0 ]; then
    error "Exiting with return value 6"
    BEFOREUPLOAD=$(date +%s)
    info "Execution time: $(expr ${BEFOREUPLOAD} - ${AFTERDOWNLOAD}) seconds"
    stopLog application_execution
    cleanup
    exit 6
fi

BEFOREUPLOAD=$(date +%s)
stopLog application_execution

info "Execution time was $(expr ${BEFOREUPLOAD} - ${AFTERDOWNLOAD})s"


####################################################################################################

provenanceFile="$BASEDIR/$DIRNAME.sh.provenance.json"
echo $provenanceFile THIS IS FOR DEBUG ONLY
copyProvenanceFile "$provenanceFile"

startLog results_upload

# Perform service call if minor status is enabled
if [[ $minorStatusEnabled == true && $serviceCall ]]; then
    $serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 5
fi


# Extract the file names and store them in a bash array
file_names=($(jq -r '.["public-output"]["output-files"] | to_entries[] | .value["file-name"]' "$provenanceFile"))

# Remove square brackets from uploadURI (we assume UploadURI will always be a single string)
uploadURI=$(echo "$uploadURI" | sed 's/^\[//; s/\]$//')

echo EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
echo $uploadURI

#  Check if uploadURI starts with "file:/"
if [[ "$uploadURI" == file:* ]]; then
    # Get the actual file system path by removing 'file:' prefix
    dir_path="${uploadURI#file:}"
    echo $dir_path
    # Create the directory if it doesn't exist
    mkdir -p "$dir_path"
    # Check if the directory was successfully created or exists
    if [ -d "$dir_path" ]; then
        echo "Directory '$dir_path' successfully created or already exists."
    else
        echo "Failed to create directory '$dir_path'."
        exit 1 # Exit the script with an error status
    fi
fi

# Check if the array is not empty and print the results
if [ ${#file_names[@]} -eq 0 ]; then
    echo "No file names found in the output-files section."
else
    echo "File names found:"
    for file_name in "${file_names[@]}"; do
        echo "$file_name"
        
        # Define the upload path
        upload_path="${uploadURI}/${file_name}"
        
        # Generate a random string for the upload command
        random_string=$(tr -dc '[:alpha:]' < /dev/urandom 2>/dev/null | head -c 32)
        
        # Execute the upload command
        upload "$upload_path" "$random_string" "$numberOfReplicas" false
    done
fi

stopLog results_upload

startLog footer
# Perform service call if minor status is enabled
if [[ $minorStatusEnabled == true && $serviceCall ]]; then
    $serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 6
fi

cleanup

STOP=$(date +%s)
info "Stop date is ${STOP}"
TOTAL=$((STOP - START))
info "Total running time: $TOTAL seconds"
UPLOAD=$((STOP - BEFOREUPLOAD))
DOWNLOAD=$((AFTERDOWNLOAD - START))
info "Input download time: ${DOWNLOAD} seconds"
info "Execution time: $(expr $BEFOREUPLOAD - $AFTERDOWNLOAD) seconds"
info "Results upload time: ${UPLOAD} seconds"
info "Exiting with return value 0"
info "(HACK for ARC: writing it in ${DIAG})"
echo "exitcode=0" >> ${DIAG}
exit 0

stopLog footer