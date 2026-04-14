import 'package:flutter/material.dart';
import '../services/api_service.dart';

class OutfitCard extends StatelessWidget {
  final AnalyzeResult result;

  const OutfitCard({super.key, required this.result});

  @override
  Widget build(BuildContext context) {
    final isMatch = result.result == '어울림';
    final color = isMatch ? Colors.green : Colors.redAccent;

    return Card(
      elevation: 4,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(
                  isMatch ? Icons.check_circle : Icons.cancel,
                  color: color,
                  size: 32,
                ),
                const SizedBox(width: 8),
                Text(
                  result.result,
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: color,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            ClipRRect(
              borderRadius: BorderRadius.circular(5),
              child: LinearProgressIndicator(
                value: result.score / 100,
                backgroundColor: Colors.grey[200],
                valueColor: AlwaysStoppedAnimation<Color>(color),
                minHeight: 10,
              ),
            ),
            const SizedBox(height: 4),
            Text('${result.score}점', style: const TextStyle(fontSize: 16)),
            const SizedBox(height: 16),
            Text(
              result.details,
              style: const TextStyle(fontSize: 14, height: 1.6),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
