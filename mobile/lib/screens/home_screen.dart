import 'dart:typed_data';
import 'package:flutter/cupertino.dart';
import 'package:image_picker/image_picker.dart';
import '../services/api_service.dart';
import 'result_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final ImagePicker _picker = ImagePicker();
  bool _isLoading = false;
  XFile? _selectedImage;

  Future<void> _pickAndAnalyze(ImageSource source) async {
    final XFile? image = await _picker.pickImage(source: source);
    if (image == null) return;

    setState(() {
      _isLoading = true;
      _selectedImage = image;
    });
    try {
      final result = await ApiService.analyzeOutfit(image);
      if (mounted) {
        Navigator.push(
          context,
          CupertinoPageRoute(
            builder: (_) => ResultScreen(result: result, imageFile: image),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        showCupertinoDialog<void>(
          context: context,
          builder: (ctx) => CupertinoAlertDialog(
            title: const Text('오류'),
            content: Text('분석 중 오류가 발생했습니다: $e'),
            actions: [
              CupertinoDialogAction(
                child: const Text('확인'),
                onPressed: () => Navigator.pop(ctx),
              ),
            ],
          ),
        );
      }
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return CupertinoPageScaffold(
      navigationBar: const CupertinoNavigationBar(
        middle: Text('KoDQ'),
      ),
      child: SafeArea(
        child: _isLoading
            ? const Center(child: CupertinoActivityIndicator(radius: 16))
            : SingleChildScrollView(
                padding: const EdgeInsets.symmetric(
                  horizontal: 24,
                  vertical: 32,
                ),
                child: Column(
                  children: [
                    _selectedImage != null
                        ? _buildImagePreview()
                        : _buildPlaceholder(),
                    const SizedBox(height: 32),
                    const Text(
                      '오늘의 코디, 어울리나요?',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      '사진을 업로드하면 AI가 분석해드립니다',
                      style: TextStyle(color: CupertinoColors.systemGrey),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 32),
                    SizedBox(
                      width: double.infinity,
                      child: CupertinoButton.filled(
                        onPressed: () => _pickAndAnalyze(ImageSource.camera),
                        child: const Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(CupertinoIcons.camera),
                            SizedBox(width: 8),
                            Text('카메라로 촬영'),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    SizedBox(
                      width: double.infinity,
                      child: CupertinoButton.filled(
                        onPressed: () => _pickAndAnalyze(ImageSource.gallery),
                        child: const Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(CupertinoIcons.photo),
                            SizedBox(width: 8),
                            Text('갤러리에서 선택'),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
      ),
    );
  }

  Widget _buildImagePreview() {
    return FutureBuilder<Uint8List>(
      future: _selectedImage!.readAsBytes(),
      builder: (context, snapshot) {
        if (!snapshot.hasData) {
          return const SizedBox(
            height: 250,
            child: Center(child: CupertinoActivityIndicator()),
          );
        }
        return ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: Image.memory(
            snapshot.data!,
            height: 250,
            width: double.infinity,
            fit: BoxFit.cover,
          ),
        );
      },
    );
  }

  Widget _buildPlaceholder() {
    return CustomPaint(
      painter: _DashedBorderPainter(),
      child: SizedBox(
        height: 250,
        width: double.infinity,
        child: const Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              CupertinoIcons.photo,
              size: 48,
              color: CupertinoColors.systemGrey,
            ),
            SizedBox(height: 12),
            Text(
              '코디 사진을 선택하세요',
              style: TextStyle(
                color: CupertinoColors.systemGrey,
                fontSize: 16,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _DashedBorderPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    const dashWidth = 8.0;
    const dashSpace = 4.0;
    const radius = Radius.circular(12);

    final paint = Paint()
      ..color = CupertinoColors.systemGrey3
      ..strokeWidth = 1.5
      ..style = PaintingStyle.stroke;

    final path = Path()
      ..addRRect(
        RRect.fromRectAndRadius(
          Rect.fromLTWH(0, 0, size.width, size.height),
          radius,
        ),
      );

    for (final metric in path.computeMetrics()) {
      double distance = 0;
      while (distance < metric.length) {
        canvas.drawPath(
          metric.extractPath(distance, distance + dashWidth),
          paint,
        );
        distance += dashWidth + dashSpace;
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
