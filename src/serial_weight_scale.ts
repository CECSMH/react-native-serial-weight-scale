import { type Config, type Device, type ScaleResult, type ErrorType, StopBits, Parity, DataBits, BaudRate, Brand } from "./interfaces/types";
import scale_module from "./interfaces/scale_module";

class SerialWeightScale {
    private productId: number;
    private config: Config;

    constructor(productId: number, config: Config) {
        this.productId = productId;
        this.config = this.validateConfig(config)
    };

    static async listDevices(): Promise<Device[]> {
        try {
            return await scale_module.listDevices();
        } catch (error: any) {
            throw new ScaleError("serial_connection", `Failed to list devices: ${error.message}`);
        }
    }

    async connect(): Promise<void> {
        try {
            await scale_module.connect(this.productId, this.config);
        } catch (error) {
            throw this.parseNativeError(error);
        }
    }

    async readWeight(): Promise<number> {
        try {
            const result: ScaleResult = await scale_module.readWeight(this.productId);
            if (result.error) throw this.parseNativeError(result.error);
            return result.weight ?? 0;
        } catch (error) {
            throw this.parseNativeError(error);
        }
    }

    async disconnect(): Promise<void> {
        try {
            await scale_module.disconnect(this.productId);
        } catch (error) {
            throw this.parseNativeError(error);
        }
    }

    monitorWeight(callback: (weight: number) => void): () => void {
        try {
            const listener = scale_module.monitorWeight(this.productId, (result: ScaleResult) => {
                if (result.error) throw this.parseNativeError(result.error);
                callback(result.weight ?? 0);
            });
            return listener;
        } catch (error: any) {
            throw this.parseNativeError(error);
        }
    }


    private parseNativeError(error: any): ScaleError {
        if (error?.code) {
            return new ScaleError(error.code, error.message);
        }
        return new ScaleError("serial_connection", `Unexpected error: ${error.message}`);
    }

    private validateConfig(config: Config): Config {
        if (!Object.values(Brand).includes(config.brand)) {
            throw new ScaleError("invalid_scale_id", `Invalid brand: ${config.brand}. Must be one of ${Object.values(Brand).join(", ")}`);
        }
        if (!Object.values(BaudRate).includes(config.baudRate)) {
            throw new ScaleError("calibration_error", `Invalid baud rate: ${config.baudRate}. Must be one of ${Object.values(BaudRate).join(", ")}`);
        }
        if (!Object.values(DataBits).includes(config.dataBits)) {
            throw new ScaleError("invalid_response", `Invalid data bits: ${config.dataBits}. Must be one of ${Object.values(DataBits).join(", ")}`);
        }

        config.parity = config.parity || Parity.None;
        if (!Object.values(Parity).includes(config.parity)) {
            throw new ScaleError("calibration_error", `Invalid parity: ${config.parity}. Must be one of ${Object.values(Parity).join(", ")}`);
        }

        config.stopBits = config.stopBits || StopBits.One;
        if (!Object.values(StopBits).includes(config.stopBits)) {
            throw new ScaleError("invalid_response", `Invalid stop bits: ${config.stopBits}. Must be one of ${Object.values(StopBits).join(", ")}`);
        }

        config.timeout = config.timeout ?? 600;
        if (config.timeout < 100 || config.timeout > 5000) {
            throw new ScaleError("timeout", `Timeout must be between 100ms and 5000ms, but received ${config.timeout}ms.`);
        }

        config.retries = config.retries ?? 4
        if (config.retries && config.retries < 4) {
            throw new ScaleError("serial_connection",`Retries must be 4 or greater, but received ${config.retries}.`);
        }
        return config;
    };


    static async disconnectAll(): Promise<void> {
        try {
            await scale_module.disconnectAll();
        } catch (error: any) {
            throw new ScaleError("serial_connection", `Failed to disconnect all scales: ${error.message}`);
        }
    };
};

class ScaleError extends Error {
    type: ErrorType;

    constructor(type: ErrorType,  message: string) {
        super(message);
        this.type = type;
    }

    toObject(): Record<string, any> {
        return {
            type: this.type,
            message: this.message,
        };
    }
}

export default SerialWeightScale;