
import type { Config, Device, ScaleResult } from './types';
import SerialWeightScale from "../NativeSerialWeightScale";

const ScaleModule = SerialWeightScale;
const monitoring_weight_listeners: any = {};

ScaleModule.onLog((msg: string) => console.log(msg));

ScaleModule.onWeightUpdate(({ productId, result }) => {
    monitoring_weight_listeners[`monitoring_${productId}`]?.call(null, result);
})

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
        monitoring_weight_listeners[`monitoring_${productId}`] = callback;
        ScaleModule.startMonitoringWeight(productId);

        return () => {
            delete monitoring_weight_listeners[`monitoring_${productId}`];
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