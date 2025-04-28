
import type { Config, Device, ScaleResult } from './types';
import SerialWeightScale from "../NativeSerialWeightScale";

const ScaleModule = SerialWeightScale;

const monitoring_weight_listeners: any = {};
const connected_listeners: any = {};
const disconnected_listeners: any = {};
const attached_listeners: any = {};
const detached_listeners: any = {};

ScaleModule.onLog((msg: string) => console.log(msg));

ScaleModule.onWeightUpdate(({ productId, result }) => {
    monitoring_weight_listeners[`monitoring_${productId}`]?.call(null, result);
});

ScaleModule.onDeviceAttached((device) => {
    attached_listeners[`evt_${device.productId}`]?.call(null, device);
});

ScaleModule.onDeviceConnected((device) => {
    connected_listeners[`evt_${device.productId}`]?.call(null, device);
});

ScaleModule.onDeviceDisconnected((device) => {
    disconnected_listeners[`evt_${device.productId}`]?.call(null, device);
    delete monitoring_weight_listeners[`evt_${device.productId}`];
});

ScaleModule.onDeviceDetached((device) => {
    detached_listeners[`evt_${device.productId}`]?.call(null, device);
});

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
           this.stopMonitorWeight(productId);
        };
    },
    stopMonitorWeight(productId: number) {
        delete monitoring_weight_listeners[`monitoring_${productId}`];
        ScaleModule.stopMonitoringWeight(productId);
    },
    disconnect(productId: number): Promise<void> {
        return ScaleModule.disconnect(productId);
    },
    disconnectAll(): Promise<void> {
        return ScaleModule.disconnectAll();
    },
    onDeviceAttached(productId: number, callback: (device: Device) => void): () => void {
        attached_listeners[`evt_${productId}`] = callback;
        return () => { this.removeOnDeviceAttached(productId); };
    },
    onDeviceConnected(productId: number, callback: (device: Device) => void): () => void {
        connected_listeners[`evt_${productId}`] = callback;
        return () => { this.removeOnDeviceConnected(productId); };
    },
    onDeviceDisconnected(productId: number, callback: (device: Device) => void): () => void {
        disconnected_listeners[`evt_${productId}`] = callback;
        return () => { this.removeOnDeviceDisconnected(productId); };
    },
    onDeviceDetached(productId: number, callback: (device: Device) => void): () => void {
        detached_listeners[`evt_${productId}`] = callback;
        return () => { this.removeOnDeviceDetached(productId); };
    },
    removeOnDeviceAttached(productId: number): void {
        delete attached_listeners[`evt_${productId}`];
    },
    removeOnDeviceConnected(productId: number): void {
        delete connected_listeners[`evt_${productId}`];
    },
    removeOnDeviceDisconnected(productId: number): void {
        delete disconnected_listeners[`evt_${productId}`];
    },
    removeOnDeviceDetached(productId: number): void {
        delete detached_listeners[`evt_${productId}`];
    },
};