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
  connect(
    scaleId: string,
    config: {
      port: string,
      baudRate: number,
      dataBits: number,
      parity: string,
      stopBits: number,
      timeout?: number,
      retries?: number,
      brand: string,
      model?: string
    }
  ): Promise<void>;
  readWeight(scaleId: string): Promise<{ weight: number }>;
  startMonitoringWeight(scaleId: string): Promise<void>;
  stopMonitoringWeight(scaleId: string): Promise<void>;
  disconnect(scaleId: string): Promise<void>;
  disconnectAll(): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('SerialWeightScale');