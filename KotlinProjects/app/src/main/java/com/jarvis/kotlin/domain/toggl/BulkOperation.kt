package com.jarvis.kotlin.domain.toggl

import com.jarvis.kotlin.constants.BulkOperationResult
import com.jarvis.kotlin.constants.BulkOperationType

data class BulkOperation(val type: BulkOperationType, val result: BulkOperationResult)
