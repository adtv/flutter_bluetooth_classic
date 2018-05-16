package io.adtv.flutterbluetoothclassic;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.UUID;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterBluetoothClassicPlugin
 */
public class FlutterBluetoothClassicPlugin implements MethodCallHandler, RequestPermissionsResultListener  {

    private static final String TAG = "FlutterBluetoothClassicPlugin";
    private static final String NAMESPACE = "plugins.adtv.io/flutter_bluetooth_classic";
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1452;
    static final private UUID CCCD_ID = UUID.fromString("000002902-0000-1000-8000-00805f9b34fb");
    private final Registrar registrar;
    private final MethodChannel channel;
    private final EventChannel stateChannel;
    private final EventChannel scanResultChannel;
    private final EventChannel servicesDiscoveredChannel;
    private final EventChannel characteristicReadChannel;
    private final EventChannel descriptorReadChannel;
    private final BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    // Pending call and result for startScan, in the case where permissions are needed
    private MethodCall pendingCall;
    private Result pendingResult;

  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_bluetooth_classic");
    channel.setMethodCallHandler(new FlutterBluetoothClassicPlugin());
  }

    FlutterBluetoothClassicPlugin(Registrar r){
        this.registrar = r;
        this.channel = new MethodChannel(registrar.messenger(), NAMESPACE+"/methods");
        this.stateChannel = new EventChannel(registrar.messenger(), NAMESPACE+"/state");
        this.scanResultChannel = new EventChannel(registrar.messenger(), NAMESPACE+"/scanResult");
        this.servicesDiscoveredChannel = new EventChannel(registrar.messenger(), NAMESPACE+"/servicesDiscovered");
        this.characteristicReadChannel = new EventChannel(registrar.messenger(), NAMESPACE+"/characteristicRead");
        this.descriptorReadChannel = new EventChannel(registrar.messenger(), NAMESPACE+"/descriptorRead");
        this.mBluetoothManager = (BluetoothManager) r.activity().getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        channel.setMethodCallHandler(this);
        stateChannel.setStreamHandler(stateHandler);
        scanResultChannel.setStreamHandler(scanResultsHandler);
        servicesDiscoveredChannel.setStreamHandler(servicesDiscoveredHandler);
        characteristicReadChannel.setStreamHandler(characteristicReadHandler);
        descriptorReadChannel.setStreamHandler(descriptorReadHandler);

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(new BluetoothListener(), filter);
    }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    }

      if(mBluetoothAdapter == null && !"isAvailable".equals(call.method)) {
          result.error("bluetooth_unavailable", "the device does not have bluetooth", null);
          return;
      }

      switch (call.method) {
          case "startScan":
          {
              if (ContextCompat.checkSelfPermission(registrar.activity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                      != PackageManager.PERMISSION_GRANTED) {
                  ActivityCompat.requestPermissions(
                          registrar.activity(),
                          new String[] {
                                  Manifest.permission.ACCESS_COARSE_LOCATION
                          },
                          REQUEST_COARSE_LOCATION_PERMISSIONS);
                  pendingCall = call;
                  pendingResult = result;
                  break;
              }
              startScan(call, result);
              break;
          }
      }
  }

    private void startScan(MethodCall call, Result result) {
        byte[] data = call.arguments();
        Protos.ScanSettings settings;
        try {
            settings = Protos.ScanSettings.newBuilder().mergeFrom(data).build();
            // TODO startScan();
            startScan21(settings);

            result.success(null);
        } catch (Exception e) {
            result.error("startScan", e.getMessage(), e);
        }
    }

    private void startScan21(Protos.ScanSettings proto) throws IllegalStateException {

        if (mBluetoothAdapter.isDiscovering()) {
            // Bluetooth is already in modo discovery mode, we cancel to restart it again
            Log.i("[search]", "Bluetooth is already in modo discovery mode, we cancel to restart it again");
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

}
