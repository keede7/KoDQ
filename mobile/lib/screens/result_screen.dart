import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../services/api_service.dart';
import '../widgets/outfit_card.dart';

class ResultScreen extends StatelessWidget {
  final AnalyzeResult result;
  final XFile imageFile;

  const ResultScreen({
    super.key,
    required this.result,
    required this.imageFile,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('분석 결과'), centerTitle: true),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: FutureBuilder<Uint8List>(
                future: imageFile.readAsBytes(),
                builder: (context, snapshot) {
                  if (!snapshot.hasData) return const SizedBox(height: 300);
                  return Image.memory(
                    snapshot.data!,
                    height: 300,
                    fit: BoxFit.cover,
                  );
                },
              ),
            ),
            const SizedBox(height: 24),
            OutfitCard(result: result),
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('다시 분석하기'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
