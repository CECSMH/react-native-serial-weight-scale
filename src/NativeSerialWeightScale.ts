import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  listDevices(): Promise<Array<{
    name: string,
    vendorId: number,
    productId: number,
    port: string,
    hasPermission: boolean
  }>>;

  connect(productId: number, config: {
    baudRate: number,
    dataBits: number,
    parity: string,
    stopBits: number,
    timeout?: number,
    retries?: number,
    brand: string,
    model?: string
  }): Promise<void>;

  isConnected(productId: number): boolean;

  readWeight(productId: number): Promise<{ weight: number }>;

  startMonitoringWeight(productId: number): Promise<void>;

  stopMonitoringWeight(productId: number): Promise<void>;

  disconnect(productId: number): Promise<void>;

  disconnectAll(): Promise<void>;

  setOnDeviceConnected(callback: (event: {
    name: string;
    vendorId: number;
    productId: number;
    port: string;
    hasPermission: boolean;
  }) => void): void;

  setOnDeviceAttached(callback: (event: {
    name: string;
    vendorId: number;
    productId: number;
    port: string;
    hasPermission: boolean;
  }) => void): void;

  setOnDeviceDisconnected(callback: (event: {
    name: string;
    vendorId: number;
    productId: number;
    port: string;
    hasPermission: boolean;
  }) => void): void;

  setOnDeviceDetached(callback: (event: {
    name: string;
    vendorId: number;
    productId: number;
    port: string;
    hasPermission: boolean;
  }) => void): void;

  setOnWeightUpdate(callback: (event: {
    productId: number;
    result: { weight?: number; error?: string };
  }) => void): void;

  setOnLog(callback: (message: string) => void): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('SerialWeightScale');