package com.infinum.sentinel.domain.collectors

import com.infinum.sentinel.domain.Factories

internal class CollectorFactory(
    private val device: Collectors.Device,
    private val application: Collectors.Application,
    private val permissions: Collectors.Permissions,
    private val preferences: Collectors.Preferences,
    private val targetedPreferences: Collectors.TargetedPreferences,
    private val certificates: Collectors.Certificates,
    private val tools: Collectors.Tools
) : Factories.Collector {

    override fun device(): Collectors.Device = device

    override fun application(): Collectors.Application = application

    override fun permissions(): Collectors.Permissions = permissions

    override fun preferences(): Collectors.Preferences = preferences

    override fun targetedPreferences(): Collectors.TargetedPreferences = targetedPreferences

    override fun certificates(): Collectors.Certificates = certificates

    override fun tools(): Collectors.Tools = tools
}
