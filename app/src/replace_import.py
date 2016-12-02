#!/usr/bin/python
# -*- coding: utf-8 -*-
'''
批量替换import 包名
将app里的示例代码复制到新工程后，IDE会默认修改包名但没有修改import

用的re包，支持正则替换的，但这里暂时没用到
替换也是每个文件整个加到内存里，因为源码文件应该不大不会有什么问题
'''
import sys
import re
import os.path


def replace_all_files(source_root, dic):
    files = os.listdir(source_root)
    for file_name in files:
        file_path = os.path.join(source_root, file_name)
        if os.path.isdir(file_path):
            replace_all_files(file_path, dic)
        elif file_path.endswith(".java"):
            fn = open(file_path, 'r')
            f = fn.read()
            fn.close()
            for i in dic.keys():
                f = re.sub(i, dic[i], f)
            fn = open(file_path, 'w')
            fn.write(f)
            fn.close()


if __name__ == "__main__":
    path = sys.argv[1]
    print("search path:"+path)
    dic = {}
    dic["import com.common.app"] = "import xxx.xxx"
    replace_all_files(path, dic)
