import 'package:flutter/material.dart';
import 'package:viewpdf/viewpdf.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _viewpdfPlugin = Viewpdf();

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Plugin example app')),
        body: Center(
          child: ElevatedButton(
            onPressed: () async {
              Map<String, dynamic> args = {
                'message': 'Hello from Flutter to PDF View, very nice!',
                'filePath': '/data/data/com.ahmed.pdfview.viewpdf_example/app_flutter/',
                'fileName': 'sample.pdf',
              };
              String? result = await _viewpdfPlugin.startPDFViewActivity(args);
              debugPrint('PDF View Activity started: $result');
            },
            child: const Text('Start PDF View Activity'),
          ),
        ),
      ),
    );
  }
}
