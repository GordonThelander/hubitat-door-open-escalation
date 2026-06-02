/**
 *  Door Left Open Escalator
 *
 *  Version: 1.6.0
 *  Author: Gordon Thelander 
 *
 *  Purpose:
 *  Watches selected contact sensors and escalates notifications the longer
 *  something remains open. Alerts stop automatically when the contact closes.
 *
 *  v1.1 scope:
 *  - Multiple contact sensors
 *  - Three escalation levels
 *  - Optional repeating final alert
 *  - Optional recovery message when closed
 *  - Optional speech announcements
 *  - Optional lights at Level 3
 *  - Optional colour setting for flashing colour bulbs
 *  - Four explicit escalation levels
 *  - Level 4 urgent escalation repeats every X minutes while contact remains open
 *  - Preferences grouped by escalation level for clearer configuration
 *  - Resolved message can also be announced on selected speech devices
 *  - Clearer section demarcation in the configuration UI
 *  - Fixed missing colour map helper and improved colour bulb compatibility
 *  - Optional separate siren step after Level 3
 *  - Optional mode filtering
 *  - Optional quiet hours
 *  - Local state tracking only
 *
 *  Install:
 *  Hubitat > Apps Code > New App > paste this file > Save
 *  Then Apps > Add User App > Door Left Open Escalator
 */

definition(
    name: "Door Left Open Escalator",
    namespace: "Hubitat_Automation",
    author: "Gordon Thelander",
    description: "Escalates alerts when doors, gates, windows, fridges, freezers or garage doors are left open.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    importUrl: ""
)

preferences {
    page(name: "mainPage", title: "Door Left Open Escalator", install: true, uninstall: true) {
        section("CONTACTS TO MONITOR") {
            paragraph "────────────────────────────────────────"
            input(
                name: "contactSensors",
                type: "capability.contactSensor",
                title: "Select contact sensors",
                multiple: true,
                required: true
            )
        }

        section("NOTIFICATION AND ANNOUNCEMENT DEVICES") {
            paragraph "────────────────────────────────────────"
            input(
                name: "notificationDevices",
                type: "capability.notification",
                title: "Send push notifications to",
                multiple: true,
                required: true
            )

            input(
                name: "speechDevices",
                type: "capability.speechSynthesis",
                title: "Optional speech devices",
                multiple: true,
                required: false
            )
        }

        section("LEVEL 1 - MENTION") {
            paragraph "────────────────────────────────────────\nSoft first reminder. No lights or siren."
            input(
                name: "level1DelayMins",
                type: "number",
                title: "Delay before Level 1 mention, in minutes",
                defaultValue: 5,
                required: true
            )

            input(
                name: "level1Message",
                type: "text",
                title: "Level 1 message",
                defaultValue: "%device% has been open for %minutes% minutes.",
                required: true
            )
        }

        section("LEVEL 2 - SERIOUS MENTION") {
            paragraph "────────────────────────────────────────\nSecond reminder with stronger wording. No lights or siren."
            input(
                name: "level2DelayMins",
                type: "number",
                title: "Delay before Level 2 serious mention, in minutes",
                defaultValue: 10,
                required: true
            )

            input(
                name: "level2Message",
                type: "text",
                title: "Level 2 message",
                defaultValue: "%device% is still open after %minutes% minutes.",
                required: true
            )
        }

        section("LEVEL 3 - WARNING WITH STEADY ALERT LIGHTS") {
            paragraph "────────────────────────────────────────\nWarning state. Selected lights turn on and stay on until resolved or Level 4 starts. Colour bulbs must be selected under the colour-capable lights field, not only under plain lights."
            input(
                name: "level3DelayMins",
                type: "number",
                title: "Delay before Level 3 warning, in minutes",
                defaultValue: 20,
                required: true
            )

            input(
                name: "level3Message",
                type: "text",
                title: "Level 3 warning message",
                defaultValue: "Warning: %device% has been left open for %minutes% minutes. Alert lights activated.",
                required: true
            )

            input(
                name: "lightDevices",
                type: "capability.switch",
                title: "Plain lights or switches to turn on at Level 3, no colour control",
                multiple: true,
                required: false
            )

            input(
                name: "colorLightDevices",
                type: "capability.colorControl",
                title: "Colour-capable lights to set at Level 3 and flash at Level 4",
                multiple: true,
                required: false
            )

            input(
                name: "level3Color",
                type: "enum",
                title: "Level 3 warning colour",
                options: [
                    "Red",
                    "Orange",
                    "Yellow",
                    "Green",
                    "Cyan",
                    "Blue",
                    "Purple",
                    "Pink",
                    "White"
                ],
                defaultValue: "Orange",
                required: false
            )

            input(
                name: "alertLightLevel",
                type: "number",
                title: "Alert light brightness level, 1-100%",
                defaultValue: 100,
                required: false
            )
        }

        section("LEVEL 4 - URGENT WARNING WITH FLASHING LIGHTS AND SIREN/STROBE") {
            paragraph "────────────────────────────────────────\nUrgent state. Lights flash, siren/strobe activates, and this level can repeat while still open."
            input(
                name: "level4DelayMins",
                type: "number",
                title: "Delay before Level 4 urgent warning, in minutes",
                description: "Must be after Level 3. Example: Level 3 at 20 minutes, Level 4 at 30 minutes.",
                defaultValue: 30,
                required: false
            )

            input(
                name: "level4Message",
                type: "text",
                title: "Level 4 urgent message",
                defaultValue: "Urgent: %device% has been left open for %minutes% minutes. Flashing lights and siren/strobe activated.",
                required: true
            )

            input(
                name: "level4Color",
                type: "enum",
                title: "Level 4 urgent flash colour",
                options: [
                    "Red",
                    "Orange",
                    "Yellow",
                    "Green",
                    "Cyan",
                    "Blue",
                    "Purple",
                    "Pink",
                    "White"
                ],
                defaultValue: "Red",
                required: false
            )

            input(
                name: "level4FlashCount",
                type: "number",
                title: "Level 4 number of light flashes",
                defaultValue: 5,
                required: false
            )

            input(
                name: "sirenDevices",
                type: "capability.alarm",
                title: "Optional sirens or strobes for Level 4",
                multiple: true,
                required: false
            )

            input(
                name: "sirenMode",
                type: "enum",
                title: "Level 4 siren/strobe action",
                options: ["siren", "strobe", "both"],
                defaultValue: "strobe",
                required: false
            )

            input(
                name: "sirenAutoStopSeconds",
                type: "number",
                title: "Stop siren/strobe after X seconds",
                defaultValue: 20,
                required: false
            )

            input(
                name: "repeatLevel4",
                type: "bool",
                title: "Repeat Level 4 while still open?",
                defaultValue: true,
                required: false
            )

            input(
                name: "level4RepeatMins",
                type: "number",
                title: "Repeat Level 4 every X minutes",
                defaultValue: 10,
                required: false
            )
        }

        section("RESOLVED / CLOSED BEHAVIOUR") {
            paragraph "────────────────────────────────────────\nWhen the contact closes, alert lights turn off, siren/strobe stops, and resolved messages are sent."
            input(
                name: "recoveryEnabled",
                type: "bool",
                title: "Send resolved message when closed?",
                defaultValue: true,
                required: true
            )

            input(
                name: "recoveryMessage",
                type: "text",
                title: "Resolved message",
                defaultValue: "%device% closed after %minutes% minutes. Alert activity stopped.",
                required: false
            )

            input(
                name: "speakRecoveryMessage",
                type: "bool",
                title: "Announce resolved message on selected speech devices?",
                defaultValue: true,
                required: false
            )
        }

        section("OPTIONAL RESTRICTIONS") {
            paragraph "────────────────────────────────────────"
            input(
                name: "enabledModes",
                type: "mode",
                title: "Only send alerts in these modes",
                multiple: true,
                required: false
            )

            input(
                name: "quietStart",
                type: "time",
                title: "Quiet hours start",
                required: false
            )

            input(
                name: "quietEnd",
                type: "time",
                title: "Quiet hours end",
                required: false
            )

            input(
                name: "suppressSpeechDuringQuietHours",
                type: "bool",
                title: "Suppress speech during quiet hours?",
                defaultValue: true,
                required: false
            )

            input(
                name: "suppressPushDuringQuietHours",
                type: "bool",
                title: "Suppress push notifications during quiet hours?",
                defaultValue: false,
                required: false
            )
        }

        section("DIAGNOSTICS") {
            paragraph "────────────────────────────────────────"
            input(
                name: "logEnable",
                type: "bool",
                title: "Enable debug logging?",
                defaultValue: false,
                required: true
            )
        }
    }
}

def installed() {
    log.info "${app.name} installed"
    initialise()
}

def updated() {
    log.info "${app.name} updated"
    unsubscribe()
    unschedule()
    initialise()
}

def initialise() {
    state.openSince = state.openSince ?: [:]
    state.currentLevel = state.currentLevel ?: [:]
    state.lastRepeat = state.lastRepeat ?: [:]
    state.level4LastFired = state.level4LastFired ?: [:]

    subscribe(contactSensors, "contact", contactHandler)

    // Rebuild state for contacts already open at app install/update.
    contactSensors?.each { device ->
        if (device.currentValue("contact") == "open") {
            markOpen(device)
        }
    }

    runIn(60, "monitorOpenContacts")
    if (logEnable) {
        runIn(1800, "disableDebugLogging")
    }
}

def disableDebugLogging() {
    app.updateSetting("logEnable", [value: "false", type: "bool"])
    log.info "Debug logging disabled automatically"
}

def contactHandler(evt) {
    def device = evt.device
    def value = evt.value

    if (logEnable) {
        log.debug "Contact event: ${device.displayName} is ${value}"
    }

    if (value == "open") {
        markOpen(device)
        runIn(60, "monitorOpenContacts")
    } else if (value == "closed") {
        markClosed(device)
    }
}

def markOpen(device) {
    String id = device.id.toString()

    if (!state.openSince?.containsKey(id)) {
        state.openSince[id] = now()
        state.currentLevel[id] = 0
        state.lastRepeat[id] = 0
        state.level4LastFired[id] = 0

        if (logEnable) {
            log.debug "${device.displayName} marked open at ${state.openSince[id]}"
        }
    }
}

def markClosed(device) {
    String id = device.id.toString()

    if (state.openSince?.containsKey(id)) {
        Long openedAt = safeLong(state.openSince[id])
        Long elapsedMins = Math.max(0L, ((now() - openedAt) / 60000L).toLong())

        if (recoveryEnabled) {
            String msg = formatMessage(recoveryMessage ?: "%device% closed after %minutes% minutes. Alert activity stopped.", device, elapsedMins)
            sendPush(msg)

            Boolean quiet = inQuietHours()
            Boolean speechSuppressed = quiet && (suppressSpeechDuringQuietHours != false)

            if (speakRecoveryMessage != false && !speechSuppressed) {
                sendSpeech(msg)
            } else if (logEnable && speechDevices && speechSuppressed) {
                log.debug "Resolved speech message suppressed by quiet hours: ${msg}"
            }
        }

        stopAllAlertActivity()
        state.openSince.remove(id)
        state.currentLevel.remove(id)
        state.lastRepeat.remove(id)
        state.level4LastFired.remove(id)

        if (logEnable) {
            log.debug "${device.displayName} closed after ${elapsedMins} minutes. State cleared."
        }
    }
}

def monitorOpenContacts() {
    Boolean anyOpen = false

    contactSensors?.each { device ->
        String id = device.id.toString()
        String contactState = device.currentValue("contact")

        if (contactState == "open") {
            anyOpen = true

            if (!state.openSince?.containsKey(id)) {
                markOpen(device)
            }

            evaluateEscalation(device)
        } else {
            // Defensive cleanup if the contact closed but the close event was missed.
            if (state.openSince?.containsKey(id)) {
                if (logEnable) {
                    log.debug "${device.displayName} is closed during monitor cycle. Cleaning stale state."
                }
                stopAllAlertActivity()
                state.openSince.remove(id)
                state.currentLevel.remove(id)
                state.lastRepeat.remove(id)
                state.level4LastFired.remove(id)
            }
        }
    }

    if (anyOpen) {
        runIn(60, "monitorOpenContacts")
    }
}

def evaluateEscalation(device) {
    String id = device.id.toString()
    Long openedAt = safeLong(state.openSince[id])
    Long elapsedMins = Math.max(0L, ((now() - openedAt) / 60000L).toLong())

    Integer current = safeInt(state.currentLevel[id])
    Integer target = calculateTargetLevel(elapsedMins)

    if (target > current) {
        sendEscalation(device, target, elapsedMins)
        state.currentLevel[id] = target

        if (target == 4) {
            state.level4LastFired[id] = now()
        }

        return
    }

    if (target == 4 && repeatLevel4 == true) {
        Integer repeatMins = getLevel4RepeatMins()
        Long last = safeLong(state.level4LastFired[id])

        if (last == 0L || (now() - last) >= repeatMins * 60000L) {
            sendEscalation(device, 4, elapsedMins)
            state.level4LastFired[id] = now()
        }
    }
}

Integer calculateTargetLevel(Long elapsedMins) {
    Integer l1 = getLevel1DelayMins()
    Integer l2 = getLevel2DelayMins()
    Integer l3 = getLevel3DelayMins()
    Integer l4 = getLevel4DelayMins()

    if (elapsedMins >= l4) {
        return 4
    }

    if (elapsedMins >= l3) {
        return 3
    }

    if (elapsedMins >= l2) {
        return 2
    }

    if (elapsedMins >= l1) {
        return 1
    }

    return 0
}

def sendEscalation(device, Integer level, Long elapsedMins) {
    if (!modeAllowed()) {
        if (logEnable) {
            log.debug "Alert suppressed by mode restriction. Current mode: ${location.mode}"
        }
        return
    }

    Boolean quiet = inQuietHours()
    Boolean pushSuppressed = quiet && (suppressPushDuringQuietHours == true)
    Boolean speechSuppressed = quiet && (suppressSpeechDuringQuietHours != false)

    String msg

    if (level == 1) {
        msg = formatMessage(level1Message, device, elapsedMins)
    } else if (level == 2) {
        msg = formatMessage(level2Message, device, elapsedMins)
    } else if (level == 3) {
        msg = formatMessage(level3Message, device, elapsedMins)
    } else {
        msg = formatMessage(level4Message, device, elapsedMins)
    }

    if (!pushSuppressed) {
        sendPush(msg)
    } else if (logEnable) {
        log.debug "Push notification suppressed by quiet hours: ${msg}"
    }

    if (!speechSuppressed) {
        sendSpeech(msg)
    } else if (logEnable && speechDevices) {
        log.debug "Speech suppressed by quiet hours: ${msg}"
    }

    if (level == 3) {
        activateLevel3Lights()
    }

    if (level == 4) {
        activateLevel4Urgent()
    }

    if (logEnable) {
        log.debug "Level ${level} alert processed for ${device.displayName}: ${msg}"
    }
}

def activateLevel3Lights() {
    if (!lightDevices && !colorLightDevices) {
        return
    }

    lightDevices?.each { device ->
        try {
            device.on()
        } catch (Exception e) {
            log.warn "Failed to turn on ${device.displayName}: ${e.message}"
        }
    }

    colorLightDevices?.each { device ->
        try {
            setAlertColor(device, level3Color ?: "Orange")
        } catch (Exception e) {
            log.warn "Failed to set Level 3 colour on ${device.displayName}: ${e.message}"
            try {
                device.on()
            } catch (Exception ignored) {
                // Already logged the colour failure.
            }
        }
    }

    if (logEnable) {
        log.debug "Level 3 lights activated using colour ${level3Color ?: 'Orange'}"
    }
}

def activateLevel4Urgent() {
    activateLevel4FlashingLights()
    activateSirenOrStrobe()
}

def activateLevel4FlashingLights() {
    if (!lightDevices && !colorLightDevices) {
        return
    }

    Integer count = Math.max(1, safeInt(level4FlashCount ?: 5))
    state.lightFlashRemaining = count * 2

    if (logEnable) {
        log.debug "Starting Level 4 flashing light sequence: ${count} flashes using colour ${level4Color ?: 'Red'}"
    }

    flashLightStep()
}

def flashLightStep() {
    Integer remaining = safeInt(state.lightFlashRemaining)

    if (remaining <= 0) {
        state.remove("lightFlashRemaining")

        // After the urgent flash sequence, leave the Level 4 colour on as the unresolved visual state.
        lightDevices?.each { device ->
            try {
                device.on()
            } catch (Exception e) {
                log.warn "Failed to leave ${device.displayName} on after Level 4 flash: ${e.message}"
            }
        }

        colorLightDevices?.each { device ->
            try {
                setAlertColor(device, level4Color ?: "Red")
            } catch (Exception e) {
                log.warn "Failed to leave ${device.displayName} on after Level 4 flash: ${e.message}"
            }
        }

        return
    }

    Boolean turnOn = (remaining % 2 == 0)

    lightDevices?.each { device ->
        try {
            if (turnOn) {
                device.on()
            } else {
                device.off()
            }
        } catch (Exception e) {
            log.warn "Failed during Level 4 light flash on ${device.displayName}: ${e.message}"
        }
    }

    colorLightDevices?.each { device ->
        try {
            if (turnOn) {
                setAlertColor(device, level4Color ?: "Red")
            } else {
                device.off()
            }
        } catch (Exception e) {
            log.warn "Failed during Level 4 colour light flash on ${device.displayName}: ${e.message}"
        }
    }

    state.lightFlashRemaining = remaining - 1
    runIn(2, "flashLightStep")
}

def activateSirenOrStrobe() {
    if (!sirenDevices) {
        return
    }

    String action = sirenMode ?: "strobe"

    sirenDevices.each { deviceAlarm ->
        try {
            if (action == "siren") {
                deviceAlarm.siren()
            } else if (action == "both") {
                deviceAlarm.both()
            } else {
                deviceAlarm.strobe()
            }
        } catch (Exception e) {
            log.warn "Failed to activate siren/strobe device ${deviceAlarm.displayName}: ${e.message}"
        }
    }

    Integer seconds = Math.max(5, safeInt(sirenAutoStopSeconds ?: 20))
    runIn(seconds, "stopSirens")

    if (logEnable) {
        log.debug "Level 4 siren/strobe activated for ${seconds} seconds"
    }
}

def stopAllAlertActivity() {
    stopSirens()
    stopLightFlashing()
    turnAlertLightsOff()
}

def stopLightFlashing() {
    state.remove("lightFlashRemaining")
}

def turnAlertLightsOff() {
    lightDevices?.each { device ->
        try {
            device.off()
        } catch (Exception e) {
            log.warn "Failed to turn off alert light ${device.displayName}: ${e.message}"
        }
    }

    colorLightDevices?.each { device ->
        try {
            device.off()
        } catch (Exception e) {
            log.warn "Failed to turn off colour alert light ${device.displayName}: ${e.message}"
        }
    }
}

def stopSirens() {
    sirenDevices?.each { device ->
        try {
            device.off()
        } catch (Exception e) {
            log.warn "Failed to stop siren/strobe device ${device.displayName}: ${e.message}"
        }
    }
}



def setAlertColor(device, String colourName) {
    Map colour = getColorMap(colourName)
    Integer level = safeInt(colour.level)
    Integer hue = safeInt(colour.hue)
    Integer saturation = safeInt(colour.saturation)

    Boolean applied = false

    try {
        // Main Hubitat ColorControl command.
        device.setColor([hue: hue, saturation: saturation, level: level])
        applied = true
    } catch (Exception e) {
        if (logEnable) {
            log.debug "setColor failed for ${device.displayName}: ${e.message}"
        }
    }

    try {
        // Compatibility path for drivers that behave better with individual commands.
        if (device.hasCommand("setLevel")) {
            device.setLevel(level)
        }
    } catch (Exception e) {
        if (logEnable) {
            log.debug "setLevel failed for ${device.displayName}: ${e.message}"
        }
    }

    try {
        if (device.hasCommand("setHue")) {
            device.setHue(hue)
            applied = true
        }
    } catch (Exception e) {
        if (logEnable) {
            log.debug "setHue failed for ${device.displayName}: ${e.message}"
        }
    }

    try {
        if (device.hasCommand("setSaturation")) {
            device.setSaturation(saturation)
            applied = true
        }
    } catch (Exception e) {
        if (logEnable) {
            log.debug "setSaturation failed for ${device.displayName}: ${e.message}"
        }
    }

    try {
        // Some bulbs do not visibly apply colour while off unless explicitly switched on.
        if (device.hasCommand("on")) {
            device.on()
        }
    } catch (Exception e) {
        log.warn "Failed to turn on colour device ${device.displayName}: ${e.message}"
    }

    if (!applied) {
        log.warn "Could not apply colour ${colourName} to ${device.displayName}. Driver may not fully support Hubitat ColorControl commands."
    } else if (logEnable) {
        log.debug "Applied colour ${colourName} to ${device.displayName}: hue=${hue}, saturation=${saturation}, level=${level}"
    }
}

Map getColorMap(String colourName) {
    Integer hue = 0
    Integer saturation = 100
    Integer level = clampLevel(alertLightLevel ?: 100)

    switch ((colourName ?: "Red").toString()) {
        case "Orange":
            hue = 8
            saturation = 100
            break
        case "Yellow":
            hue = 17
            saturation = 100
            break
        case "Green":
            hue = 33
            saturation = 100
            break
        case "Cyan":
            hue = 50
            saturation = 100
            break
        case "Blue":
            hue = 66
            saturation = 100
            break
        case "Purple":
            hue = 75
            saturation = 100
            break
        case "Pink":
            hue = 90
            saturation = 70
            break
        case "White":
            hue = 0
            saturation = 0
            break
        case "Red":
        default:
            hue = 0
            saturation = 100
            break
    }

    return [hue: hue, saturation: saturation, level: level]
}

Integer clampLevel(value) {
    Integer level = safeInt(value)

    if (level < 1) {
        return 1
    }

    if (level > 100) {
        return 100
    }

    return level
}


def sendPush(String msg) {
    notificationDevices?.each { device ->
        try {
            device.deviceNotification(msg)
        } catch (Exception e) {
            log.warn "Failed to send notification to ${device.displayName}: ${e.message}"
        }
    }
}

def sendSpeech(String msg) {
    speechDevices?.each { device ->
        try {
            device.speak(msg)
        } catch (Exception e) {
            log.warn "Failed to speak on ${device.displayName}: ${e.message}"
        }
    }
}

String formatMessage(String template, device, Long elapsedMins) {
    String deviceName = device.displayName ?: "Contact sensor"
    String msg = template ?: "%device% has been open for %minutes% minutes."

    msg = msg.replace("%device%", deviceName)
    msg = msg.replace("%minutes%", elapsedMins.toString())
    msg = msg.replace("%mode%", location.mode ?: "unknown")

    return msg
}

Boolean modeAllowed() {
    if (!enabledModes) {
        return true
    }

    return enabledModes.contains(location.mode)
}

Boolean inQuietHours() {
    if (!quietStart || !quietEnd) {
        return false
    }

    Date nowDate = new Date()
    Date start = timeToday(quietStart, location.timeZone)
    Date end = timeToday(quietEnd, location.timeZone)

    if (start.before(end)) {
        return nowDate.after(start) && nowDate.before(end)
    }

    // Handles quiet windows that cross midnight, e.g. 22:00 to 06:00.
    return nowDate.after(start) || nowDate.before(end)
}

Integer getLevel1DelayMins() {
    return Math.max(1, safeInt(level1DelayMins ?: 5))
}

Integer getLevel2DelayMins() {
    return Math.max(getLevel1DelayMins(), safeInt(level2DelayMins ?: 10))
}

Integer getLevel3DelayMins() {
    return Math.max(getLevel2DelayMins(), safeInt(level3DelayMins ?: 20))
}

Integer getLevel4DelayMins() {
    return Math.max(getLevel3DelayMins(), safeInt(level4DelayMins ?: 30))
}

Integer getLevel4RepeatMins() {
    return Math.max(1, safeInt(level4RepeatMins ?: 10))
}

Integer safeInt(value) {
    try {
        return value as Integer
    } catch (Exception ignored) {
        return 0
    }
}

Long safeLong(value) {
    try {
        return value as Long
    } catch (Exception ignored) {
        return 0L
    }
}
