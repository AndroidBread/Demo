# shellcheck disable=SC2162
# webP类型图片转换脚本，用户转换webp到png，jpg，gif等

read -p "输入要转换的文件/文件夹（绝对路径）：" path
echo "处理中。。。"

if [ -f "$path" ]; then
  read -p "请选择要输出的格式，默认.png\n：1 .png \t 2 .jpg \t 3 .git" num
  echo "开始文件转换"
  path1=$(dirname "$path")
  path2="$path1/convert"
  mkdir path2
  if [  -"1" == "$num" ]; then
      dwebp path -o "$path2/convert.png"
  elif [ -"2" == $num ]; then
      dwebp path -o "$path2/convert.jpg"
  elif [ -"3" == $num ]; then
      dwebp path -o "$path2/convert.gif"
  else
      dwebp path -o "$path2/convert.png"
  fi
  echo "转换完成！！"
  exit
fi

if [ -d "$path" ]; then
  read -p "请选择要输出的格式，默认.png\n：1 .png \t 2 .jpg \t 3 .git" num1
  echo "开始文件转换"
  path3=$(ls "$path")
  path4="$path/convert"
  mkdir "$path4"
  countNum=1
  for entry in $path3
  do
    filePath="$path/$entry"
    echo "$filePath"
    echo "$countNum"
    #dwebp /Users/moongod/Desktop/biaoqingbao102/tgtowabot2.webp -o /Users/moongod/Desktop/biaoqingbao102/convert/convert1.png
    if [  -"1" == "$num1" ]; then
          dwebp "$filePath" -o "$path4/convert$countNum.png"
      elif [ -"2" == "$num1" ]; then
          dwebp "$filePath" -o "$path4/convert$countNum.jpg"
      elif [ -"3" == "$num1" ]; then
          dwebp "$filePath" -o "$path4/convert$countNum.gif"
      else
          dwebp "$filePath" -o "$path4/convert$countNum.png"
      fi
      # shellcheck disable=SC2219
      let countNum++
  done
  echo "转换完成！！"
  exit
fi
echo "啥也不是！！！"


