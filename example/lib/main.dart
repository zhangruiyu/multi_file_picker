import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:multi_file_picker/multi_file_picker.dart';
import 'package:permission_handler/permission_handler.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    //检测摄像头,存储
    Permission.mediaLibrary;
    var isAgree = (await [
      Permission.storage,
    ].request())
        .values
        .every((item) => item == PermissionStatus.granted);
    if (isAgree) {
      var platformVersion;
      // Platform messages may fail, so we use a try/catch PlatformException.
      try {
        platformVersion = await MultiFilePicker.select();
      } on PlatformException {
        platformVersion = 'Failed to get platform version.';
      }

      // If the widget was removed from the tree while the asynchronous platform
      // message was in flight, we want to discard the reply rather than calling
      // setState to update our non-existent appearance.
      if (!mounted) return;

      setState(() {
        _platformVersion = platformVersion.toString();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: GestureDetector(
              onTap: () {
                initPlatformState();
              },
              child: Text('Running on: $_platformVersion\n')),
        ),
      ),
    );
  }
}
