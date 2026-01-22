# SDR IRSS API Simulation

A Java-based implementation of the International Radio Security Services (IRSS) API for Software Defined Radio (SDR) systems. This project demonstrates cryptographic operations, key management, and secure channel communications based on the WINNF-09-S-0011 specification.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [API Operations Implemented](#api-operations-implemented)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

This project simulates a client-server architecture for Software Defined Radio security services. It implements:

- **Cryptographic Channels**: AES/DES encryption and decryption
- **Hash Channels**: SHA-256/SHA-512 integrity verification
- **Key Management**: Secure key storage and lifecycle management
- **Channel Management**: Create, configure, and destroy communication channels

## ✨ Features

- ✅ Full client-server architecture using Java sockets
- ✅ AES-128/256 and DES encryption algorithms
- ✅ SHA-256, SHA-512, MD5, SHA-1 hashing
- ✅ Secure key storage and management
- ✅ Thread-safe concurrent operations
- ✅ Interactive demonstration mode
- ✅ Comprehensive error handling

## 🔧 Prerequisites

### Required Software

1. **Java Development Kit (JDK)**
   - Version: JDK 8 or higher (JDK 11 recommended)
   - Download from: https://www.oracle.com/java/technologies/downloads/

2. **Visual Studio Code**
   - Download from: https://code.visualstudio.com/

3. **VS Code Extensions** (Install from Extensions marketplace):
   - Extension Pack for Java (by Microsoft)
   - Language Support for Java (by Red Hat)

### Verify Installation

Open terminal/command prompt and check:

```bash
# Check Java version
java -version

# Check Java compiler
javac -version
```

You should see version 8 or higher.

## 📥 Installation

### Step 1: Set Up Project Directory

1. Create project folder:
```bash
mkdir sdr-irss-simulation
cd sdr-irss-simulation
```

2. Create the directory structure:
```bash
mkdir -p src/main/java/com/sdr/server/impl
mkdir -p src/main/java/com/sdr/client
mkdir bin
```

### Step 2: Copy Source Files

Copy all `.java` files into their respective directories:

```
sdr-irss-simulation/
├── src/main/java/com/sdr/
│   ├── Main.java
│   ├── server/
│   │   ├── CryptoSubsystem.java
│   │   ├── IRSSServer.java
│   │   └── impl/
│   │       ├── CryptoChannelImpl.java
│   │       ├── HashChannelImpl.java
│   │       └── KeyStoreImpl.java
│   └── client/
│       ├── WaveformClient.java
│       └── IRSSClientImpl.java
└── bin/
```

### Step 3: Open in VS Code

1. Open VS Code
2. File → Open Folder → Select `sdr-irss-simulation`
3. Wait for Java extension to activate (bottom right corner)

## 🚀 Running the Application

### Method 1: Using VS Code (Recommended for Beginners)

#### Option A: Integrated Mode (Easiest)

1. Open `Main.java` in VS Code
2. Click the **Run** button (▶️) at the top right
3. In the terminal, choose option **3** (Start Both)
4. Watch the automated demonstration

#### Option B: Separate Server and Client

**Terminal 1 (Server):**
1. Open `Main.java`
2. Click Run ▶️
3. Choose option **1** (Start Server)
4. Wait for "Waiting for client connections..."

**Terminal 2 (Client):**
1. Open new terminal: Terminal → New Terminal
2. Type:
```bash
cd src/main/java
java com.sdr.Main
```
3. Choose option **2** (Start Client)

### Method 2: Using Command Line

#### Step 1: Compile All Files

```bash
# Navigate to project root
cd sdr-irss-simulation

# Compile all Java files
javac -d bin src/main/java/com/sdr/*.java src/main/java/com/sdr/server/*.java src/main/java/com/sdr/server/impl/*.java src/main/java/com/sdr/client/*.java
```

#### Step 2: Run the Application

**Windows:**
```bash
# Integrated mode
java -cp bin com.sdr.Main
# Then choose option 3

# OR run server and client separately:
# Terminal 1:
java -cp bin com.sdr.Main
# Choose option 1

# Terminal 2:
java -cp bin com.sdr.Main
# Choose option 2
```

**Mac/Linux:**
```bash
# Integrated mode
java -cp bin com.sdr.Main
# Then choose option 3

# OR run server and client separately:
# Terminal 1:
java -cp bin com.sdr.Main
# Choose option 1

# Terminal 2:
java -cp bin com.sdr.Main
# Choose option 2
```

## 📁 Project Structure

```
sdr-irss-simulation/
│
├── src/main/java/com/sdr/
│   │
│   ├── Main.java                    # Entry point, menu system
│   │
│   ├── server/
│   │   ├── CryptoSubsystem.java     # Core crypto engine
│   │   ├── IRSSServer.java          # Network server
│   │   └── impl/
│   │       ├── CryptoChannelImpl.java   # Encryption/decryption
│   │       ├── HashChannelImpl.java     # Hash generation
│   │       └── KeyStoreImpl.java        # Key management
│   │
│   └── client/
│       ├── WaveformClient.java      # Main client
│       └── IRSSClientImpl.java      # API wrapper
│
├── bin/                             # Compiled .class files
└── README.md                        # This file
```

## 📚 API Operations Implemented

### 1. Channel Management (IRSS::Control::ChannelMgmt)

| Operation | Description | Status |
|-----------|-------------|--------|
| createCryptographicChannel | Create encryption/decryption channel | ✅ |
| createHashChannel | Create integrity check channel | ✅ |
| destroyChannel | Remove channel and free resources | ✅ |
| addConfiguration | Add crypto configuration to channel | ✅ |
| activateConfiguration | Activate a configuration | ✅ |

### 2. Cryptographic Operations (IRSS::Infosec)

| Operation | Description | Status |
|-----------|-------------|--------|
| transformPackets (encrypt) | Encrypt data packets | ✅ |
| transformPackets (decrypt) | Decrypt data packets | ✅ |
| transformStream | Stream encryption/decryption | ✅ |

### 3. Key Management (IRSS::Control::KeyMgmt)

| Operation | Description | Status |
|-----------|-------------|--------|
| storeKey | Store encryption key | ✅ |
| updateKey | Update/rotate key | ✅ |
| getUpdateCount | Get key update count | ✅ |
| zeroizeKey | Securely delete key | ✅ |

### 4. Integrity & Authentication (IRSS::IandA)

| Operation | Description | Status |
|-----------|-------------|--------|
| generateHash | Create cryptographic hash | ✅ |
| pushData | Add data to hash | ✅ |
| getHash | Retrieve computed hash | ✅ |
| reset | Reset hash channel | ✅ |

## 🎬 Expected Output

When running in integrated mode (option 3), you should see:

```
╔════════════════════════════════════════════════════════╗
║    SDR IRSS API Simulation System                     ║
║    International Radio Security Services              ║
╚════════════════════════════════════════════════════════╝

[STARTING INTEGRATED MODE]

╔════════════════════════════════════════════════╗
║  IRSS Server Started                           ║
║  Port: 8080                                    ║
║  Waiting for client connections...            ║
╚════════════════════════════════════════════════╝

[CSS] Cryptographic Subsystem initialized
[KEYSTORE] Initialized with 3 default keys

[CLIENT] Connecting to localhost:8080
[CLIENT] Connected successfully

╔════════════════════════════════════════════════════════╗
║           IRSS API Demonstration                       ║
╚════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────┐
│ Demo 1: Cryptographic Channel                  │
└─────────────────────────────────────────────────┘

1. Creating cryptographic channel...
   ✓ Channel created: 1

2. Storing encryption key...
   ✓ Key stored: 4

3. Adding channel configuration...
   ✓ Configuration added: 1

4. Activating configuration...
   ✓ Configuration activated

5. Encrypting data...
   Plaintext: "Hello, Secure Radio World!"
   ✓ Data encrypted (32 bytes)

6. Decrypting data...
   Decrypted: "Hello, Secure Radio World!"
   ✓ Verification: SUCCESS

7. Destroying channel...
   ✓ Channel destroyed

... (additional demos)
```

## 🐛 Troubleshooting

### Problem: "java: command not found"

**Solution:**
1. Install JDK (see Prerequisites)
2. Add Java to PATH:
   - Windows: System Properties → Environment Variables → Edit PATH
   - Mac/Linux: Add to `.bashrc` or `.zshrc`:
     ```bash
     export PATH="/path/to/jdk/bin:$PATH"
     ```

### Problem: "Cannot find symbol" errors

**Solution:**
1. Ensure all files are in correct directories
2. Recompile with full path:
```bash
javac -d bin src/main/java/com/sdr/**/*.java
```

### Problem: "Port 8080 already in use"

**Solution:**
1. Change port in `IRSSServer.java`:
```java
private static final int DEFAULT_PORT = 8081; // Change to different port
```
2. Update client connection in `Main.java`

### Problem: VS Code doesn't show Run button

**Solution:**
1. Install "Extension Pack for Java"
2. Reload VS Code: Ctrl+Shift+P → "Reload Window"
3. Wait for Java extension to activate (check bottom right)

### Problem: "ClassNotFoundException"

**Solution:**
Run from correct directory:
```bash
cd sdr-irss-simulation
java -cp bin com.sdr.Main
```

## 📖 Understanding the Code

### Key Components:

1. **Main.java**: Entry point, provides menu for different modes
2. **IRSSServer.java**: Network server handling client requests
3. **CryptoSubsystem.java**: Core crypto engine managing channels
4. **WaveformClient.java**: Client demonstrating API usage
5. **Implementation Classes**: Actual crypto/hash/key operations

### Flow of Operations:

```
Client Request → Network → Server → CryptoSubsystem → Crypto/Hash Channel
                                                    ↓
Client Response ← Network ← Server ← Result ← Operation Complete
```

## 🎓 Learning Path

1. **Start Simple**: Run option 3 (integrated mode) to see everything work
2. **Examine Output**: Read the console to understand the flow
3. **Modify Demo**: Edit `WaveformClient.runDemo()` to try different operations
4. **Explore Code**: Read through each implementation file
5. **Experiment**: Add new algorithms or operations

## 📞 Support

If you encounter issues:

1. Check this README thoroughly
2. Verify all prerequisites are installed
3. Ensure files are in correct directories
4. Check console output for specific error messages

## 📄 License

Educational project based on WINNF-09-S-0011 IRSS API Specification.

## 🙏 Acknowledgments

Based on:
- WINNF-09-S-0011 International Radio Security Services API Specification
- SCA (Software Communications Architecture) Specification Version 4.1

---

**Quick Start Reminder:**

```bash
# Compile
javac -d bin src/main/java/com/sdr/**/*.java

# Run
java -cp bin com.sdr.Main

# Choose option 3 for full demo
```