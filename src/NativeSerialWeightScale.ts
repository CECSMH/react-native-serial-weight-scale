import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type { EventEmitter } from 'react-native/Libraries/Types/CodegenTypes';
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

  readWeight(productId: number): Promise<{ weight: number }>;

  startMonitoringWeight(productId: number): Promise<void>;

  stopMonitoringWeight(productId: number): Promise<void>;

  disconnect(productId: number): Promise<void>;

  disconnectAll(): Promise<void>;

  readonly onDeviceConnected: EventEmitter<{
    name: string,
    vendorId: number,
    productId: number,
    port: string,
    hasPermission: boolean
  }>
  readonly onDeviceDisconnected: EventEmitter<{
    name: string,
    vendorId: number,
    productId: number,
    port: string,
    hasPermission: boolean
  }>
  readonly onWeightUpdate: EventEmitter<{ productId: number; result: { weight?: number; error?: string } }>;
  readonly onLog: EventEmitter<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('SerialWeightScale');