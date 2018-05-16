import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_bluetooth_classic/constants.dart';

class FlutterBluetoothClassic {

  final MethodChannel _channel = const MethodChannel('$NAMESPACE/methods');
  final EventChannel _stateChannel = const EventChannel('$NAMESPACE/state');
  final EventChannel _scanResultChannel =
  const EventChannel('$NAMESPACE/scanResult');
  final EventChannel _servicesDiscoveredChannel =
  const EventChannel('$NAMESPACE/servicesDiscovered');
  final EventChannel _characteristicReadChannel =
  const EventChannel('$NAMESPACE/characteristicRead');
  final EventChannel _descriptorReadChannel =
  const EventChannel('$NAMESPACE/descriptorRead');
  final StreamController<MethodCall> _methodStreamController =
  new StreamController.broadcast(); // ignore: close_sinks
  Stream<MethodCall> get _methodStream => _methodStreamController
      .stream; // Used internally to dispatch methods from platform.

  FlutterBluetoothClassic._() {
    _channel.setMethodCallHandler(MethodCall call) {
      _methodStreamController.add(call);
    }
  }
  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
