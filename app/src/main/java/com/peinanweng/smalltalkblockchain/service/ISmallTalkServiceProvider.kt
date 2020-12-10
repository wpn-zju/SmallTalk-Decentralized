package com.peinanweng.smalltalkblockchain.service

interface ISmallTalkServiceProvider {
    fun hasService(): Boolean
    fun getService(): ISmallTalkService?
}
