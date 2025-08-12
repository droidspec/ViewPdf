import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'viewpdf_method_channel.dart';

abstract class ViewpdfPlatform extends PlatformInterface {
  /// Constructs a ViewpdfPlatform.
  ViewpdfPlatform() : super(token: _token);

  static final Object _token = Object();

  static ViewpdfPlatform _instance = MethodChannelViewpdf();

  /// The default instance of [ViewpdfPlatform] to use.
  ///
  /// Defaults to [MethodChannelViewpdf].
  static ViewpdfPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ViewpdfPlatform] when
  /// they register themselves.
  static set instance(ViewpdfPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
  Future<String?> startPDFViewActivity(Map<String, dynamic> args) {
    throw UnimplementedError('startPDFViewActivity() has not been implemented.');
  }
}
