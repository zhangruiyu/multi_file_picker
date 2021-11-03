import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class MultiFilePicker {
  static const MethodChannel _channel = MethodChannel('multi_file_picker');

  static Future<List<String>> select({List<String>? type}) async {
    if (Platform.isAndroid) {
      return (await _channel.invokeListMethod<String>('select', {
        'type': type ?? ["aac", "mp3", "wav", "m4a", "flac"]
      }))!;
    } else {
      return [];
    }
  }
}
