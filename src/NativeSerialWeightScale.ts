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
}

export default TurboModuleRegistry.getEnforcing<Spec>('SerialWeightScale');