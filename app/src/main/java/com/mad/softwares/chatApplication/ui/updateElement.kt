package com.mad.softwares.chatApplication.ui

fun <T> updateElement(list: List<T>, newElement: T, index:Int): List<T> {
//    return list.mapIndexed{i,element->
//        if(i==index) newElement else element
//    }
    if(index in list.indices){
        return list.subList(0,index)+newElement+list.subList(index+1,list.size)
    }
    else{
        return list
    }
}