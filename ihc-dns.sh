#!/bin/bash

COOKIE_JAR=/tmp/ihc-cookie.txt
DNS_PAGE=/tmp/ihc-dns.html
V_USER=
V_PASSWORD=
V_ACTION=
V_DOMAIN=
V_NAME=
V_TYPE=
V_CONTENT=
V_RECORD=

################################################################################

function auth() {
    # Авторизация

    local __login=$1
    local __password=$2

    local __result=$(curl -s -X POST 'https://my.ihc.ru/j_spring_security_check?ajax=true' \
        --cookie-jar $COOKIE_JAR \
        -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' \
        -H 'Referer: https://my.ihc.ru/login/auth' \
        --data-raw "j_username=${__login}&j_password=${__password}&recaptcha=&ihccaptcha=" \
        | jq -r '.alert.type')

    if [[ $__result != 'none' ]]; then
        echo 'ERROR AUTH'
        exit 1
    fi
}

function get_domains() {
    # Получить список доступных доменов
    # RETURN
    #   {
    #     "id": 1234567,
    #     "domain": "example.com"
    #   }

    curl -s -X GET 'https://my.ihc.ru/dnsZone/list' \
        --cookie $COOKIE_JAR \
        --cookie-jar $COOKIE_JAR \
        > $DNS_PAGE

    xmllint --html --xpath '//li[@class="zoneList__zone "]/*/a' $DNS_PAGE 2>/dev/null \
        | perl -ne 'chop($_); m#href="/dnsZone/index/(.+?)">(.+?)</a>#; print "{\"id\": $1, \"domain\": \"$2\"}\n"' \
        | jq
}

function get_domain() {
    # RETURN
    #   {
    #     "id": 1234567,
    #     "domain": "example.com"
    #   }

    local __domain=$1

    get_domains | jq '. | select(.domain == "'$__domain'")'
}

function get_domain_records() {
    local __domain_id=$1

    curl -s -X POST \
        --cookie $COOKIE_JAR \
        --cookie-jar $COOKIE_JAR \
        -H 'Accept: application/json, text/javascript, */*; q=0.01' \
        -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' \
        -H 'Referer: https://my.ihc.ru/dnsZone/index/'$__domain_id \
        --data-raw 'id='$__domain_id \
        'https://my.ihc.ru/dnsZone/records' \
        | jq '.data.records[]'
}

function create_record() {
    local __domain_id=$1
    local __type=$2
    local __content=$3
    local __name=$4

    curl -s -X POST \
        --cookie $COOKIE_JAR \
        --cookie-jar $COOKIE_JAR \
        -H 'Accept: application/json, text/javascript, */*; q=0.01' \
        -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' \
        -H 'Referer: https://my.ihc.ru/dnsZone/index/'$__domain_id \
        -H 'X-Requested-With: XMLHttpRequest' \
        --data-raw 'name='$__name'&type='$__type'&content='$__content'&id='$__domain_id \
        'https://my.ihc.ru/dnsZone/createRecord'
}

function edit_record() {
    local __domain_id=$1
    local __record_id=$2
    local __type=$3
    local __content=$4
    local __name=$5

    curl -s -X POST \
        --cookie $COOKIE_JAR \
        --cookie-jar $COOKIE_JAR \
        -H 'Accept: application/json, text/javascript, */*; q=0.01' \
        -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' \
        -H 'Referer: https://my.ihc.ru/dnsZone/index/'$__domain_id \
        -H 'X-Requested-With: XMLHttpRequest' \
        --data-raw 'name='$__name'&content='$__content'&id='$__domain_id'&recordId='$__record_id'&type='$__type \
        'https://my.ihc.ru/dnsZone/updateRecord'
}

function delete_record() {
    local __domain_id=$1
    local __record_id=$2

    curl -s -X POST \
        --cookie $COOKIE_JAR \
        --cookie-jar $COOKIE_JAR \
        -H 'Accept: application/json, text/javascript, */*; q=0.01' \
        -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' \
        -H 'Referer: https://my.ihc.ru/dnsZone/index/'$__domain_id \
        -H 'X-Requested-With: XMLHttpRequest' \
        --data-raw 'id='$__domain_id'&recordId='$__record_id \
        'https://my.ihc.ru/dnsZone/deleteRecord'
}

function clean() {
    rm $COOKIE_JAR
    rm $DNS_PAGE
}

################################################################################

function action_list() {
    if [[ -z "$V_DOMAIN" ]]; then
        auth "$V_USER" "$V_PASSWORD"
        get_domains
        clean
    else
        auth "$V_USER" "$V_PASSWORD"
        get_domain_records $(get_domains | jq -r '. | select(.domain == "'$V_DOMAIN'") | .id')
        clean
    fi
}

function action_add() {
    if [[ -z "$V_DOMAIN" ]]; then
        echo "Требуется указать домен"
        exit 2
    fi

    if [[ -z "$V_TYPE" ]]; then
        echo "Требуется указать тип контента"
        exit 2
    fi

    if [[ -z "$V_CONTENT" ]]; then
        echo "Требуется указать контент"
        exit 2
    fi

    auth "$V_USER" "$V_PASSWORD"
    local __domain_id=$(get_domains | jq -r '. | select(.domain == "'$V_DOMAIN'") | .id')
    create_record $__domain_id "$V_TYPE" "$V_CONTENT" "$V_NAME" | jq
    clean
}

function action_delete() {
    if [[ -z "$V_DOMAIN" ]]; then
        echo "Требуется указать домен"
        exit 3
    fi

    if [[ -z "$V_RECORD" ]]; then
        echo "Требуется указать ID записи"
        exit 3
    fi

    auth "$V_USER" "$V_PASSWORD"
    local __domain_id=$(get_domains | jq -r '. | select(.domain == "'$V_DOMAIN'") | .id')
    delete_record $__domain_id $V_RECORD | jq
    clean
}

function action_edit() {
    if [[ -z "$V_DOMAIN" ]]; then
        echo "Требуется указать домен"
        exit 4
    fi

    if [[ -z "$V_RECORD" ]]; then
        echo "Требуется указать ID записи"
        exit 4
    fi

    if [[ -z "$V_TYPE" ]]; then
        echo "Требуется указать тип контента"
        exit 4
    fi

    if [[ -z "$V_CONTENT" ]]; then
        echo "Требуется указать контент"
        exit 4
    fi

    auth "$V_USER" "$V_PASSWORD"
    local __domain_id=$(get_domains | jq -r '. | select(.domain == "'$V_DOMAIN'") | .id')
    edit_record $__domain_id $V_RECORD "$V_TYPE" "$V_CONTENT" "$V_NAME" | jq
    clean
}

function entry_point() {
    if [[ -z "$V_USER" ]] || [[ -z "$V_PASSWORD" ]]; then
        echo "Требуется указать логин и пароль"
        exit 1
    fi

    if [[ -z "$V_ACTION" ]]; then
        echo "Треуется действие"
        exit 1
    fi

    case "$V_ACTION" in
        "list") action_list ;;
        "add") action_add ;;
        "edit") action_edit ;;
        "delete") action_delete ;;
    esac
}

################################################################################

# https://www.baeldung.com/linux/bash-parse-command-line-arguments

VALID_ARGS=$(getopt -o '' --long user:,password:,domain:,list,add,edit,name:,type:,content:,delete,record: -- "$@")
if [[ $? -ne 0 ]]; then
    exit 1;
fi

eval set -- "$VALID_ARGS"

while [ : ]; do
    case "$1" in
        --user)
            V_USER=$2
            shift 2
            ;;
        --password)
            V_PASSWORD=$2
            shift 2
            ;;
        --domain)
            V_DOMAIN=$2
            shift 2
            ;;
        --list)
            V_ACTION="list"
            shift
            ;;
        --add)
            V_ACTION="add"
            shift
            ;;
        --edit)
            V_ACTION="edit"
            shift
            ;;
        --name)
            V_NAME=$2
            shift 2
            ;;
        --type)
            V_TYPE=$2
            shift 2
            ;;
        --content)
            V_CONTENT=$2
            shift 2
            ;;
        --delete)
            V_ACTION="delete"
            shift
            ;;
        --record)
            V_RECORD=$2
            shift 2
            ;;
        --)
            shift
            break
            ;;
    esac
done

entry_point

