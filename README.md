# react-native-serial-weight-scale

A React Native module for interfacing with serial weight scales. This library provides a TypeScript-compatible API to connect, read, monitor, and manage serial weight scales.
[![npm version](https://badge.fury.io/js/react-native-serial-weight-scale.svg)](https://badge.fury.io/js/react-native-serial-weight-scale) 
[![Platform](https://img.shields.io/badge/platform-Android-yellow.svg)](https://www.android.com)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Features

- **Read Weight**: Retrieve the current weight from a scale.
- **Monitor Weight**: Continuously monitor weight changes with a callback-based API.
- **Device Listing**: List all available serial devices.
- **Event Handling**: Register callbacks for device connection, disconnection, attachment, and detachment.
- **TypeScript Support**: Full type definitions for a type-safe development experience.
- **Error Handling**: Standardized `ScaleError` with specific error types for debugging.

## Installation

Install the package via npm or yarn:

```bash
npm install react-native-serial-weight-scale
```

or

```bash
yarn add react-native-serial-weight-scale
```

## Usage

The `SerialWeightScale` class is the primary interface for interacting with weight scales. Below is a complete example demonstrating device listing, weight reading, monitoring, and event handling.

**Note**: This module is compatible only with Android devices due to its reliance on Android-specific serial communication APIs.

### Example

```typescript
import SerialWeightScale, { Config, Brand, BaudRate, DataBits, Parity, StopBits } from 'react-native-serial-weight-scale';

// Define configuration
const config: Config = {
  brand: Brand.Toledo,
  baudRate: BaudRate.Standard,
  dataBits: DataBits.Eight,
  parity: Parity.None,
  stopBits: StopBits.One,
  timeout: 1000, // Optional: defaults to 600ms
  retries: 5,    // Optional: defaults to 4
};

async function useScale() {
  try {
    // List available devices
    const devices = await SerialWeightScale.listDevices();
    console.log('Available devices:', devices);

    // Initialize scale with a product ID
    const scale = new SerialWeightScale(devices[0].productId, config);

    // Register event handlers
    scale.onAttached((device) => console.log('Device attached:', device));
    scale.onDetached((device) => console.log('Device detached:', device));
    scale.onConnected((device) => console.log('Device connected:', device));
    scale.onDisconnected((device) => console.log('Device disconnected:', device));

    // Connect to the scale
    await scale.connect();
    console.log('Connected to scale');

    // Read current weight
    const weight = await scale.readWeight();
    console.log('Weight:', weight);

    // Monitor weight changes
    const stopMonitoring = scale.monitorWeight((weight) => {
      console.log('New weight:', weight);
    });

    // Stop monitoring and disconnect after 10 seconds
    setTimeout(async () => {
      stopMonitoring();
      // or scale.stopMonitorWeight();
      await scale.disconnect();
      console.log('Disconnected');
    }, 10000);
  } catch (error) {
    console.error(`Error type: ${error.type}, Message: ${error.message}`);
  }
}

useScale();
```

## Configuration

The `Config` interface defines the serial port settings for the scale. All fields are required unless specified otherwise.

```typescript
interface Config {
  brand: Brand;          // Scale brand (e.g., Toledo, Filizola)
  baudRate: BaudRate;    // Baud rate (e.g., 9600, 115200)
  dataBits: DataBits;    // Data bits (e.g., 8, 7)
  parity: Parity;        // Parity setting (e.g., None, Even, Odd)
  stopBits: StopBits;    // Stop bits (e.g., 1, 2)
  timeout?: number;      // Read timeout in ms (100–5000, defaults to 600)
  retries?: number;      // Number of retries (≥4, defaults to 4)
}
```

### Supported Values

- **Brand**:

  ```typescript
  enum Brand {
    Elgin = "elgin",
    Toledo = "toledo",
    Filizola = "filizola",
    Urano = "urano",
    Micheletti = "micheletti"
  }
  ```

- **BaudRate**:

  ```typescript
  enum BaudRate {
    Low = 2400,
    Medium = 4800,
    Standard = 9600,
    High = 115200
  }
  ```

- **DataBits**:

  ```typescript
  enum DataBits {
    Five = 5,
    Six = 6,
    Seven = 7,
    Eight = 8
  }
  ```

- **Parity**:

  ```typescript
  enum Parity {
    None = "none",
    Even = "even",
    Odd = "odd"
  }
  ```

- **StopBits**:

  ```typescript
  enum StopBits {
    One = 1,
    OnePointFive = 3,
    Two = 2
  }
  ```

## API Reference

### `SerialWeightScale` Class

#### Constructor

```typescript
constructor(productId: number, config: Config)
```

- **Parameters**:
  - `productId`: Unique identifier for the scale (obtained from `listDevices`).
  - `config`: Serial port configuration.
- **Throws**: `ScaleError` if the configuration is invalid (e.g., invalid brand, baud rate, or timeout).

#### Static Methods

- `listDevices(): Promise<Device[]>`

  - Lists available serial devices.
  - **Returns**: Array of `Device` objects with `name`, `vendorId`, `productId`, `port`, and `hasPermission`.
  - **Throws**: `ScaleError` (type: `serial_connection`).

- `disconnectAll(): Promise<void>`

  - Disconnects all connected scales.
  - **Throws**: `ScaleError` (type: `serial_connection`).

#### Instance Methods

- `connect(): Promise<void>`

  - Connects to the scale.
  - **Throws**: `ScaleError` (e.g., type: `serial_connection`).

- `isConnected(): boolean`

  - Check scale is connected

- `readWeight(): Promise<number>`

  - Reads the current weight.
  - **Returns**: Weight value (in grams) or `0` if unavailable.
  - **Throws**: `ScaleError` (e.g., type: `serial_connection`, `invalid_response`).

- `disconnect(): Promise<void>`

  - Disconnects the scale.
  - **Throws**: `ScaleError` (type: `serial_connection`).

- `monitorWeight(callback: (weight: number) => void): () => void`

  - Monitors weight changes, calling the callback with each new weight.
  - **Returns**: Function to stop monitoring.
  - **Throws**: `ScaleError` (e.g., type: `serial_connection`).

- `stopMonitorWeight(): void`

  - Stops weight monitoring for the scale.

- `onConnected(callback: (device: Device) => void): () => void`

  - Registers a callback for when the device is connected.
  - **Returns**: Function to remove the event listener.

- `onDisconnected(callback: (device: Device) => void): () => void`

  - Registers a callback for when the device is disconnected.
  - **Returns**: Function to remove the event listener.

- `onAttached(callback: (device: Device) => void): () => void`

  - Registers a callback for when the device is attached (plugged in).
  - **Returns**: Function to remove the event listener.

- `onDetached(callback: (device: Device) => void): () => void`

  - Registers a callback for when the device is detached (unplugged).
  - **Returns**: Function to remove the event listener.

- `removeConnectedEvent(): void`

  - Removes the connection event listener.

- `removeDisconnectedEvent(): void`

  - Removes the disconnection event listener.

- `removeAttachedEvent(): void`

  - Removes the attachment event listener.

- `removeDetachedEvent(): void`

  - Removes the detachment event listener.

## Error Handling

Errors are thrown as `ScaleError` instances with the following structure:

```typescript
interface ScaleError {
  type: ErrorType;
  message: string;
}
```

### Error Types

```typescript
type ErrorType =
  | 'unstable_weight'      // Weight reading is unstable
  | 'negative_weight'      // Negative weight detected
  | 'timeout'              // Operation timed out
  | 'overload'             // Scale is overloaded
  | 'zero_capture'         // Issue with zero capture
  | 'calibration_error'    // Calibration issue
  | 'invalid_response'     // Invalid response from scale
  | 'serial_connection'    // Serial connection issue
  | 'invalid_scale_id';    // Invalid scale ID or brand
```

### Example Error Handling

```typescript
try {
  await scale.connect();
} catch (error) {
  console.error(`Error type: ${error.type}, Message: ${error.message}`);
}
```

## Requirements

- **Platform**: Android only (due to dependency on Android-specific serial communication APIs)
- React Native ≥ 0.60
- Node.js ≥ 14

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m 'Add your feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

Please include tests and update documentation as needed.

## License

MIT License. See LICENSE for details.

## Support

For issues or questions, open an issue on the GitHub repository or contact the maintainers.