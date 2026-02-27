package com.example.compensation.domain.exception

import com.example.compensation.domain.CompensationStatus

class InvalidCompensationStateException(current: CompensationStatus, target: CompensationStatus) :
    IllegalStateException("Cannot transition from $current to $target")
