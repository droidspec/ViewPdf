
import 'viewpdf_platform_interface.dart';

class Viewpdf {
  Future<String?> getPlatformVersion() {
    return ViewpdfPlatform.instance.getPlatformVersion();
  }
  Future<String?> startPDFViewActivity(Map<String, dynamic> args) {
    return ViewpdfPlatform.instance.startPDFViewActivity(args);
  }
}
