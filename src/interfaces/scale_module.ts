import { NativeEventEmitter } from 'react-native';
import type { Config, Device, ScaleResult } from './types';
import SerialWeightScale from "../NativeSerialWeightScale";

const ScaleModule = SerialWeightScale as any;
const eventEmitter = new NativeEventEmitter(ScaleModule);

export default {
    listDevices(): Promise<Device[]> {
        return ScaleModule.listDevices();
    },
    connect(productId: number, config: Config): Promise<void> {
        return ScaleModule.connect(productId, config);
    },
    readWeight(productId: number): Promise<ScaleResult> {
        return ScaleModule.readWeight(productId);
    },
    monitorWeight(productId: number, callback: (result: ScaleResult) => void): () => void {
        const listener = eventEmitter.addListener('WeightUpdate', (event: { productId: number; result: ScaleResult }) => {
            if (event.productId === productId) {
                callback(event.result);
            }
        });
        ScaleModule.startMonitoringWeight(productId);
        return () => {
            listener.remove();
            ScaleModule.stopMonitoringWeight(productId);
        };
    },
    disconnect(productId: number): Promise<void> {
        return ScaleModule.disconnect(productId);
    },
    disconnectAll(): Promise<void> {
        return ScaleModule.disconnectAll();
    },
};