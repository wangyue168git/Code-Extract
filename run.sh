#!/bin/bash

# 定义默认参数
extract_filepath="/path/to/extract/file"
filetype="java"
outputFile="code.txt"

# 处理命令行参数
while getopts "hf:t:o:" opt; do
  case $opt in
    h)
      echo "Usage: run.sh [-f extract_filepath] [-t filetype]"
      echo "  -f <extract_filepath>  project path to extract files (default: /path/to/extract/file)"
      echo "  -t <filetype>          type of files to extract (default: java)"
      exit 0
      ;;
    f)
      extract_filepath=$OPTARG
      ;;
    t)
      filetype=$OPTARG
      ;;
    o)
      outputFile=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
  esac
done

# 执行 java 命令
java -jar -Dextract.filepath="$extract_filepath" -Dfiletype="$filetype" -DoutputFile="$outputFile" CodeExtract.jar