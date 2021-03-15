# crash-spotter
Minecraft mod that looks at crash reports and tries to guess what mods were involved in a crash.

## Features:
- Adds markers into a stacktrace indicating methods injected from Mixins
- Adds a section to the crash report that shows:
  - What modids and names were referenced in the stacktrace.
  - What mixin-injected methods were referenced in the stacktrace
