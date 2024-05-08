# shellcheck disable=SC2162
read -p "输入要生成的文件夹根目录（绝对路径）：" path
read -p "输入模块名称：" moduleName
echo "处理中。。。"

path1="$path/readme.txt"
if [ ! -f "$path1" ]; then
touch "$path1"
echo -e "自动生成文件夹目录:\n ui:\t存放对应的MVP文件\n network：\t存放网络请求层编码文件\n util: \t存放工具类（utils）、帮助类（helper）\n widget: \t存放对应自定义View" > "$path1"
echo "$path/empty.kt创建空文件夹完成！"
else
echo "文件夹已存在"
fi

mkdir -p "$path"/{ui,network,util,model,presenter,contract}
echo "跟目录创建完成"

path2="$path/network"
mkdir -p "$path2"/{api,entity}
echo "network 子目录创建完成！"


path3="$path/network/"$moduleName"Service.kt"
if [ ! -f "$path3" ]; then
touch "$path3"
echo "$path3 创建文件完成！"
else
echo "文件已存在"
fi

path4="$path/ui"
mkdir -p "$path4"/{adapter,dialog,widget}
echo "network 子目录创建完成！"


path5="$path/ui/"$moduleName"Fragment.kt"
if [ ! -f "$path5" ]; then
touch "$path5"
echo "$path5 创建文件完成！"
else
echo "文件夹已存在"
fi


echo "脚本执行完毕，文件夹目录创建成功"




