import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:image_picker/image_picker.dart';

class AnalyzeResult {
  final String result;
  final int score;
  final String details;

  AnalyzeResult({
    required this.result,
    required this.score,
    required this.details,
  });

  factory AnalyzeResult.fromJson(Map<String, dynamic> json) {
    return AnalyzeResult(
      result: json['result'] as String,
      score: json['score'] as int,
      details: json['details'] as String,
    );
  }
}

class ApiService {
  // 개발 환경: localhost, 배포 후 실제 서버 URL로 교체
  static const String _baseUrl = 'http://localhost:8080';

  static Future<AnalyzeResult> analyzeOutfit(XFile imageFile) async {
    final uri = Uri.parse('$_baseUrl/api/analyze');
    final request = http.MultipartRequest('POST', uri);
    final bytes = await imageFile.readAsBytes();
    request.files.add(http.MultipartFile.fromBytes('image', bytes, filename: imageFile.name));

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);

    if (response.statusCode != 200) {
      throw Exception('서버 오류: ${response.statusCode}');
    }

    final json = jsonDecode(response.body) as Map<String, dynamic>;
    return AnalyzeResult.fromJson(json);
  }
}
