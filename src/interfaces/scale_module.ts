import { NativeModules, NativeEventEmitter } from 'react-native';
import type { Config, Device, ScaleResult } from './types';

const ScaleModule = NativeModules.ScaleModule;
const eventEmitter = new NativeEventEmitter(ScaleModule);

export default {
    listDevices(): Promise<Device[]> {
        return ScaleModule.listDevices();
    },
    connect(scaleId: string, config: Config): Promise<void> {
        return ScaleModule.connect(scaleId, config);
    },
    readWeight(scaleId: string): Promise<ScaleResult> {
        return ScaleModule.readWeight(scaleId);
    },
    monitorWeight(scaleId: string, callback: (result: ScaleResult) => void): () => void {
        const listener = eventEmitter.addListener('WeightUpdate', (event: { scaleId: string; result: ScaleResult }) => {
            if (event.scaleId === scaleId) {
                callback(event.result);
            }
        });
        ScaleModule.startMonitoringWeight(scaleId);
        return () => {
            listener.remove();
            ScaleModule.stopMonitoringWeight(scaleId);
        };
    },
    disconnect(scaleId: string): Promise<void> {
        return ScaleModule.disconnect(scaleId);
    },
    disconnectAll(): Promise<void> {
        return ScaleModule.disconnectAll();
    },
};