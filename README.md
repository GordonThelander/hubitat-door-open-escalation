# Door Left Open Escalator

A Hubitat app that watches contact sensors and escalates alerts the longer a door, gate, window, fridge, freezer or garage door remains open.

Unlike a basic "door left open" notification, this app uses four escalation levels. It starts with a soft mention, becomes more serious over time, then moves to visual warning lights and finally urgent flashing lights with an optional siren or strobe.

## Current Version

| Item | Details |
|---|---|
| Version | 1.6.0 |
| Platform | Hubitat Elevation |
| Language | Groovy |
| App type | User App |
| Primary use | Escalating contact sensor alerts |
| Latest change | Fixed colour-capable light handling and improved compatibility across Hubitat colour bulb drivers |

## What It Does

| Level | Default delay | Behaviour |
|---:|---:|---|
| 1 | 5 min | Sends a soft mention |
| 2 | 10 min | Sends a more serious mention |
| 3 | 20 min | Sends a warning and turns selected lights on in a selected colour |
| 4 | 30 min | Sends an urgent warning, flashes selected lights in a selected colour, and activates an optional siren/strobe |
| Resolved | On close | Stops siren/strobe, stops flashing, turns alert lights off, and sends a resolved message |

Level 4 can repeat every configured number of minutes while the contact remains open.

## Example Use Cases

| Use case | Why it helps |
|---|---|
| Garage door left open | Security and weather exposure |
| Side gate left open | Pets, access control and security |
| Pool gate left open | Safety risk |
| Fridge or freezer left open | Food spoilage prevention |
| External door left open at night | Security and air conditioning loss |
| Window left open | Weather, wind and air conditioning loss |

## Key Features

| Feature | Supported |
|---|---:|
| Multiple contact sensors | Yes |
| Push notifications | Yes |
| Speech announcements | Yes |
| Resolved speech announcement | Yes |
| Four configurable escalation levels | Yes |
| Per-level delay settings | Yes |
| Per-level message settings | Yes |
| Level 3 steady alert lights | Yes |
| Level 3 selected colour | Yes |
| Level 4 flashing alert lights | Yes |
| Level 4 selected colour | Yes |
| Optional siren/strobe at Level 4 | Yes |
| Siren/strobe auto-stop after X seconds | Yes |
| Level 4 repeat every X minutes | Yes |
| Mode restrictions | Yes |
| Quiet hours | Yes |
| Debug logging | Yes |

## Installation

| Step | Action |
|---:|---|
| 1 | In Hubitat, go to **Apps Code** |
| 2 | Select **New App** |
| 3 | Paste the contents of `Door_Left_Open_Escalator_v1_6.groovy` |
| 4 | Click **Save** |
| 5 | Go to **Apps** |
| 6 | Select **Add User App** |
| 7 | Choose **Door Left Open Escalator** |
| 8 | Configure sensors, notifications and escalation levels |

## Configuration Sections

The app configuration is grouped by escalation level so the behaviour is easier to understand.

| Section | Purpose |
|---|---|
| Contacts to monitor | Select the contact sensors to watch |
| Notification and announcement devices | Select push notification and optional speech devices |
| Level 1 - Mention | Configure first delay and soft message |
| Level 2 - Serious Mention | Configure second delay and stronger message |
| Level 3 - Warning with Steady Alert Lights | Configure warning delay, message, lights, colour and brightness |
| Level 4 - Urgent Warning with Flashing Lights and Siren/Strobe | Configure urgent delay, message, flash colour, flash count, siren/strobe and repeat behaviour |
| Resolved / Closed Behaviour | Configure resolved message and optional speech announcement |
| Optional Restrictions | Configure mode and quiet-hour restrictions |
| Diagnostics | Enable or disable debug logging |

## Recommended Defaults

| Setting | Recommended value |
|---|---:|
| Level 1 delay | 5 min |
| Level 2 delay | 10 min |
| Level 3 delay | 20 min |
| Level 4 delay | 30 min |
| Level 3 colour | Orange |
| Level 4 colour | Red |
| Alert light brightness | 100% |
| Level 4 flash count | 5 |
| Siren/strobe action | Strobe |
| Siren/strobe auto-stop | 20 sec |
| Level 4 repeat | Enabled |
| Level 4 repeat interval | 10 min |

## Colour Light Notes

Colour-capable bulbs must be selected in the **Colour-capable lights** field, not only in the plain lights/switches field.

| Field | Use it for |
|---|---|
| Plain lights or switches | Simple on/off devices with no colour control |
| Colour-capable lights | Bulbs or light strips that support Hubitat `ColorControl` |
| Level 3 colour | Steady warning colour |
| Level 4 colour | Urgent flashing colour |

Version 1.6 improves colour compatibility by trying several Hubitat commands:

| Command | Purpose |
|---|---|
| `setColor()` | Main colour control command |
| `setLevel()` | Sets brightness |
| `setHue()` | Compatibility fallback |
| `setSaturation()` | Compatibility fallback |
| `on()` | Ensures the bulb visibly turns on after colour is applied |

## Quiet Hours

Quiet hours can suppress push notifications and/or speech announcements.

| Option | Behaviour |
|---|---|
| Suppress speech during quiet hours | Prevents speech announcements during the quiet window |
| Suppress push during quiet hours | Prevents push messages during the quiet window |
| Siren/strobe | Still follows Level 4 logic unless the app is restricted by mode |

## Mode Restrictions

You can restrict alerts to selected Hubitat modes.

| Example | Behaviour |
|---|---|
| Only Night and Away | Alerts only run in Night or Away mode |
| No mode selected | Alerts run in all modes |

## Message Tokens

Messages can use the following tokens.

| Token | Replaced with |
|---|---|
| `%device%` | Contact sensor display name |
| `%minutes%` | Number of minutes the contact has been open |
| `%mode%` | Current Hubitat mode |

Example:

```text
Urgent: %device% has been left open for %minutes% minutes.
```

Could become:

```text
Urgent: Garage Door has been left open for 30 minutes.
```

## Behaviour on Close

When the contact closes, the app immediately resolves the alert state.

| Action | Result |
|---|---|
| Stop siren/strobe | Yes |
| Stop light flashing | Yes |
| Turn selected alert lights off | Yes |
| Clear internal open-state tracking | Yes |
| Send resolved push message | Optional |
| Speak resolved message | Optional |

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| Plain lights turn on but colour does not change | Colour bulb was selected only as a plain switch | Add it under **Colour-capable lights** |
| Colour still does not change | Driver may not fully support Hubitat `ColorControl` | Enable debug logging and check app logs |
| Siren does not activate | No siren selected, mode restriction active, or Level 4 not reached | Check Level 4 settings and current Hubitat mode |
| Alerts do not fire | Contact is not open long enough, mode restriction active, or quiet-hour suppression enabled | Check delays, mode and quiet-hour settings |
| Resolved message does not speak | No speech device selected, resolved speech disabled, or quiet hours suppressing speech | Check resolved and speech settings |

## Design Principles

| Principle | Implementation |
|---|---|
| Simple first | One app, no child apps |
| Explicit escalation | Four clear levels |
| Safe siren behaviour | Siren/strobe only activates at Level 4 |
| Auto resolution | Closing the contact stops all alert activity |
| Household friendly | Configuration is grouped by escalation level |
| Local-first | Runs inside Hubitat using local devices and app state |

## Limitations

| Limitation | Detail |
|---|---|
| UI styling | Hubitat app preferences do not support true shaded panels or framed sections |
| Colour support | Depends on the selected device driver supporting Hubitat colour commands |
| Per-sensor custom timing | v1.6 uses shared timings across selected sensors |
| Child app profiles | Not included in v1.6 |
| Weather awareness | Not included |
| Per-door templates | Not included |

## Suggested Future Enhancements

| Enhancement | Value |
|---|---|
| Per-contact profiles | Different timings for garage, fridge, freezer, pool gate and windows |
| Built-in templates | Quick setup for common use cases |
| Separate Level 3 and Level 4 light device selections | More granular control |
| Dashboard status tile | Shows current alert state |
| Optional repeat for Level 1/2 | Useful for low-severity reminders |
| Weather-aware escalation | Escalate faster if rain or high wind is detected |
| Presence-aware escalation | Escalate faster if nobody is home |
| HSM integration | Optional security-mode escalation |

## File

| File | Description |
|---|---|
| `Door_Left_Open_Escalator.groovy` | Hubitat Groovy app source code |

## Licence

No licence has been applied yet. Add one before publishing publicly.

Common options:

| Licence | Use case |
|---|---|
| MIT | Simple permissive open-source licence |
| Apache 2.0 | Permissive licence with explicit patent language |
| GPLv3 | Strong copyleft licence |

## Short Description

Door Left Open Escalator is a Hubitat app that escalates alerts when a contact sensor remains open, moving from soft notifications to warning lights, urgent flashing lights and optional siren/strobe activation, then automatically resolves everything when the contact closes.
