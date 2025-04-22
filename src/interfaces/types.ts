export type ErrorType =
    | 'unstable_weight'
    | 'negative_weight'
    | 'timeout'
    | 'overload'
    | 'zero_capture'
    | 'calibration_error'
    | 'invalid_response'
    | 'serial_connection'
    | 'invalid_scale_id';

export interface Device {
    name: string;
    vendorId: number;
    productId: number;
    port: string;
    hasPermission: boolean;
}

export interface Config {
    brand: 'toledo' | 'filizola' | 'urano' | 'micheletti';
    model?: string;
    port: string;
    baudRate: number;
    dataBits: number;
    parity: 'none' | 'even' | 'odd';
    stopBits: number;
    timeout?: number;
    retries?: number;
}

export interface ScaleError {
    type: ErrorType;
    code: number;
    message: string;
    rawResponse?: string;
}

export interface ScaleResult {
    weight?: number;
    error?: ScaleError;
}