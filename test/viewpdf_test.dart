import 'package:flutter_test/flutter_test.dart';
import 'package:viewpdf/viewpdf.dart';
import 'package:viewpdf/viewpdf_platform_interface.dart';
import 'package:viewpdf/viewpdf_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockViewpdfPlatform
    with MockPlatformInterfaceMixin
    implements ViewpdfPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
  
  @override
  Future<String?> startPDFViewActivity(Map<String, dynamic> args) {
    throw UnimplementedError();
  }
}

void main() {
  final ViewpdfPlatform initialPlatform = ViewpdfPlatform.instance;

  test('$MethodChannelViewpdf is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelViewpdf>());
  });

  test('getPlatformVersion', () async {
    Viewpdf viewpdfPlugin = Viewpdf();
    MockViewpdfPlatform fakePlatform = MockViewpdfPlatform();
    ViewpdfPlatform.instance = fakePlatform;

    expect(await viewpdfPlugin.getPlatformVersion(), '42');
  });
}
