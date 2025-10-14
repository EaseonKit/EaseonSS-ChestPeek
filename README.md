## 🌿 Easeon - ChestPeek
**Minecraft:** `1.21.10`, `1.21.9`  
**Loader:** `Fabric`  
**Side:** `Server-Side`, `Singleplayer`

## Overview
ChestPeek is a server-side Fabric mod that allows players to access chests directly through item frames or wall signs attached to them.  
Simply click on a decorative item frame or sign placed on a chest to instantly open it without removing your decorations.  
Perfect for organized storage systems and aesthetic builds.

## Features
- **Click-Through Item Frames**: Right-click item frames to access chests behind them
- **Wall Sign Integration**: Click wall signs to open containers behind them
- **Ender Chest Compatible**: Works with ender chests and all container types
- **Shift Override**: Hold Shift to interact with frames/signs normally
- **Server-Side Only**: No client installation required

## Commands
All commands require OP level 2 permission.

**View Current Status:**
```
/easeon chestpeek
```
**Enable ChestPeek:**
```
/easeon chestpeek on
```
**Disable ChestPeek:**
```
/easeon chestpeek off
```

## Configuration
```json
{
  "enabled": true,
  "requiredOpLevel": 2 // Requires a server restart to take effect.
}
```
`config/easeon/easeon.ss.chestpeek.json`
