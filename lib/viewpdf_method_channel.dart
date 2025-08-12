import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'viewpdf_platform_interface.dart';

/// An implementation of [ViewpdfPlatform] that uses method channels.
class MethodChannelViewpdf extends ViewpdfPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('viewpdf');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
  @override
  Future<String?> startPDFViewActivity(Map<String, dynamic> args) async {
    final result = await methodChannel.invokeMethod<String>('startPDFViewActivity', {'data': args});
    return result;
  }
}
