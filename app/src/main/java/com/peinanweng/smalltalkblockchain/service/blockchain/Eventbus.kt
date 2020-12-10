package com.peinanweng.smalltalkblockchain.service.blockchain

class InvalidEvent

class InsertChannelEvent(val channel: String)

class DeleteChannelEvent(val channel: String)

class InsertMessageEvent(val channel: String, val sender: String, val message: String, val isText: Boolean)
