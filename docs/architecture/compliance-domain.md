# Compliance domain

## Overview

The compliance engine evaluates electronic monitoring telemetry against versioned compliance rules.

Rules are defined and versioned in code, while rule configuration allows operational parameters, such as battery-level thresholds, to be changed independently.

The current implementation evaluates battery-level telemetry and maintains the current compliance state for electronic monitoring devices.

## Current domain model

`DeviceCompliance` is currently the aggregate root for the compliance state of a device.

A `DeviceCompliance` contains a `DeviceRuleCompliance` for each applicable rule definition.

An activated device is considered compliant only when it is compliant with all applicable rules. A rule that has not received sufficient data to evaluate has a state of `NO_DATA`, which currently causes the device's overall state to be `NON_COMPLIANT`.

A deactivated device has no current overall compliance state.

## Known modelling limitation: device reuse

The current model associates compliance with a physical device using `DeviceId`.

In the wider electronic monitoring domain, a device activation represents the allocation of a device to a person. Physical devices can subsequently be deactivated and reused for another person.

This means that `DeviceCompliance` may not ultimately be the correct aggregate boundary. Compliance is likely to apply to a particular device activation rather than to the physical device for its entire lifetime.

## Future consideration

Before implementing device reactivation behaviour or long-term compliance history, revisit the aggregate identity.

A future model may resemble:

```text
DeviceActivationCompliance
  activationId
  deviceId
  status
  overall compliance
  rule compliance[]
```

A new activation would then naturally initialise each applicable rule with `NO_DATA`.

Until this is resolved:

* `DeviceCompliance` remains keyed by `DeviceId`.
* Device status uses the existing `ACTIVATED` / `DEACTIVATED` terminology.
* Do not automatically reset rule compliance when a device changes from `DEACTIVATED` to `ACTIVATED`.
* Do not assume that historical compliance for a `DeviceId` belongs to a single monitoring period.
* Revisit this model before implementing historical compliance or production handling of device reuse.

## Current persistence model

`DeviceCompliance` is persisted as the aggregate root, with `DeviceRuleCompliance` persisted as child entities.

```text
device_compliance
  |
  +-- device_rule_compliance
  +-- device_rule_compliance
  +-- ...
```

There is one current `DeviceCompliance` record per known device and one child record per applicable rule definition.

This persistence model reflects the current POC domain model and may need to change if compliance becomes associated with device activations.
