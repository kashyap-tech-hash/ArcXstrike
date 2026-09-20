<div align="center">

![ARCXSTRIKE Banner](assets/banner.png)

# ⚡ ARCXSTRIKE

### Next-Generation Rootless Arch Linux Mobile Security & Research Suite for Android
*Rootless Arch Linux ARM64 Engine · Cyberpunk Hacker Terminal · Network & Wireless Auditing*

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-00FF66.svg?style=flat-square)](LICENSE)
[![Platform: Android](https://img.shields.io/badge/Platform-Android%207.0%2B-00FF66.svg?style=flat-square)](https://developer.android.com)
[![Engine: Arch Linux ARM64](https://img.shields.io/badge/Engine-Arch%20Linux%20ARM64-1793D1.svg?style=flat-square)](https://archlinuxarm.org)
[![Based on: Stryker](https://img.shields.io/badge/Based%20on-Stryker%20App-brightgreen.svg?style=flat-square)](https://github.com/zalexdev/strykerapp)

---

</div>

## 📌 About ARCXSTRIKE

**ARCXSTRIKE** is an advanced mobile security auditing and penetration testing suite designed for Android. 

> [!NOTE]
> **Lineage & Heritage:**  
> **ARCXSTRIKE is based on and evolved from [Stryker App](https://github.com/zalexdev/strykerapp)** (developed by [zalexdev](https://github.com/zalexdev)). ARCXSTRIKE extends the original concept by integrating a **Rootless Arch Linux ARM64 engine** powered by QEMU, native `pacman` package management, modernized cyberpunk terminal interfaces, real-time telemetry rings, and an expanded security auditing toolkit.

ARCXSTRIKE allows security researchers, penetration testers, and students to run industry-standard auditing tools (such as Nmap, Metasploit, Nuclei, Hydra, and SearchSploit) directly on an Android device without requiring root permissions or endangering host system integrity.

---

## ⚠️ Educational & Ethical Disclaimer

> [!IMPORTANT]
> **ARCXSTRIKE is designed strictly for educational purposes, authorized security auditing, and academic research.**  
> Under no circumstances should this application or its bundled utilities be used against unauthorized networks, computers, or devices. You are solely responsible for complying with all international, national, and local cybersecurity laws and regulations. Always obtain prior written authorization before performing security assessments.

---

## 🚀 Core Features

| Feature | Description |
| :--- | :--- |
| 🛡️ **Rootless Arch Linux Engine** | Embedded Arch Linux ARM64 guest system powered by QEMU with native `pacman` package management, VirtFS shared storage (`/sdcard/ARCXSTRIKE`), and dynamic core management. |
| 💻 **Hacker Terminal** | High-performance terminal emulator with custom `⚡ARCXSTRIKE` neon green shell prompt, Arch Linux aliases, and seamless guest execution. |
| 📊 **Dashboard & VM Telemetry** | Real-time VM status ring, CPU/RAM telemetry sparklines, guest battery and storage metrics, and quick action launchpads. |
| 📡 **WiFi Security Toolkit** | Monitor nearby access points, map 2.4/5GHz channel congestion, capture 4-way WPA handshakes, and test WPS implementations (Pixie Dust, known pins) via compatible OTG Wi-Fi adapters. |
| 🤝 **Handshake Hub** | Local handshake manager (`/sdcard/ARCXSTRIKE/captured`), offline wordlist cracking via Hashcat/Aircrack, and cloud hash export. |
| 🌐 **Local Network & Nmap** | Fast subnet discovery, OS fingerprinting, port scanning, NSE script execution, and exportable XML/JSON reports. |
| 🔍 **Web Scanner (Nuclei)** | Automated ProjectDiscovery Nuclei vulnerability scanner with community template feeds and vulnerability classification. |
| 💣 **Arsenal & ExploitDB** | Parameterized script launcher (`{IP}`, `{PORT}`, `{MAC}`) and offline SearchSploit mirror for rapid proof-of-concept verification. |
| 🎯 **Metasploit Framework** | Integrated MSF RPC console, module search, staged and stageless payload generation with `msfvenom` and `msfpc`. |
| 🔌 **USB Arsenal & HID** | USB ConfigFS gadget orchestrator (HID keyboard, mass storage, RNDIS network) with DuckyScript execution engine (on supported kernels). |
| 🗺️ **GeoMac Mapping** | Offline and USGS satellite mapping of captured BSSIDs with export to KML (Google Earth) and JSON. |
| 🖥️ **VNC Desktop (XFCE)** | In-guest graphical desktop environment with XFCE4, x11vnc, and headless Xvfb rendering. |

---

## 🏗️ Architecture: Rootless Arch Linux Guest

ARCXSTRIKE implements an isolated, self-contained rootless virtualization engine:

```mermaid
graph TD
    A[Android Host System] -->|User Space| B[ARCXSTRIKE App UI & Terminal]
    B -->|Control Socket / Agent| C[Rootless QEMU ARM64 Layer]
    C -->|SLIRP User Network| D[Arch Linux ARM64 Guest OS]
    D -->|pacman| E[Security Toolchain: Nmap, Metasploit, Nuclei]
    C -->|VirtFS 9p Mount| F[/sdcard/ARCXSTRIKE Shared Storage]
```

1. **Rootless QEMU ARM64 (`qemu-system-aarch64`)**: Spawns an isolated Linux virtual machine using user-mode SLIRP networking and VirtIO storage.
2. **Arch Linux ARM64 Rootfs**: Operates a lightweight Arch Linux guest environment with `pacman` as the primary package manager.
3. **VirtFS Shared Storage**: Bidirectional 9p file-sharing between Android host storage (`/sdcard/ARCXSTRIKE`) and guest mount points.
4. **Console Agent**: Internal Unix domain socket / socat agent bridging Android Java processes with guest shell executions without requiring `root` or `adb`.

---

## 🛠️ Building from Source

### Prerequisites
* Android Studio Iguana / Jellyfish or newer
* Android SDK (API 24 to 28+) & NDK (r25c or newer)
* Java Development Kit (JDK 17)

### Build Commands

```bash
# Clone the repository
git clone https://github.com/Arunachalam-gojosaturo/ArcXstrike.git
cd ArcXstrike

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease

# Install on a connected test device
./gradlew installDebug
```

Compiled APK files will be located in `app/build/outputs/apk/`.

---

## 🤝 Credits & Acknowledgements

* **[Stryker App](https://github.com/zalexdev/strykerapp)** by **[zalexdev](https://github.com/zalexdev)** — The foundation upon which ARCXSTRIKE is built.
* **Arch Linux ARM** — [archlinuxarm.org](https://archlinuxarm.org)
* **QEMU** — [qemu.org](https://www.qemu.org)
* **Terminal Emulator** & open-source Linux utilities (BusyBox, Nmap, Metasploit, Nuclei).

---

## 📜 License

ARCXSTRIKE is licensed under the **GNU General Public License v3.0** ([GPL-3.0](LICENSE)).
Bundled third-party components (Terminal emulator, QEMU, BusyBox, Arch Linux packages) preserve their respective open-source licenses.
