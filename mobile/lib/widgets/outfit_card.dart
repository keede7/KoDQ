import 'package:flutter/cupertino.dart';
import '../services/api_service.dart';

class OutfitCard extends StatelessWidget {
  final AnalyzeResult result;

  const OutfitCard({super.key, required this.result});

  @override
  Widget build(BuildContext context) {
    final isMatch = result.result == '어울림';
    final isUnable = result.result == '분석불가';

    final Color labelColor;
    final IconData icon;

    if (isUnable) {
      labelColor = CupertinoColors.systemGrey;
      icon = CupertinoIcons.exclamationmark_circle;
    } else if (isMatch) {
      labelColor = CupertinoColors.activeGreen;
      icon = CupertinoIcons.checkmark_circle;
    } else {
      labelColor = CupertinoColors.destructiveRed;
      icon = CupertinoIcons.xmark_circle;
    }

    final Color scoreColor =
        result.score >= 70
            ? CupertinoColors.activeGreen
            : CupertinoColors.destructiveRed;

    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: CupertinoColors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: const [
          BoxShadow(
            color: Color(0x18000000),
            blurRadius: 8,
            offset: Offset(0, 2),
          ),
        ],
      ),
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, color: labelColor, size: 32),
              const SizedBox(width: 8),
              Text(
                result.result,
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: labelColor,
                ),
              ),
            ],
          ),
          if (!isUnable) ...[
            const SizedBox(height: 16),
            _ScoreBar(score: result.score, color: scoreColor),
            const SizedBox(height: 4),
            Text('${result.score}점', style: const TextStyle(fontSize: 16)),
          ],
          const SizedBox(height: 16),
          Text(
            result.details,
            style: const TextStyle(fontSize: 14, height: 1.6),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}

class _ScoreBar extends StatelessWidget {
  final int score;
  final Color color;

  const _ScoreBar({required this.score, required this.color});

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        return Stack(
          children: [
            Container(
              height: 10,
              width: constraints.maxWidth,
              decoration: BoxDecoration(
                color: CupertinoColors.systemGrey5,
                borderRadius: BorderRadius.circular(5),
              ),
            ),
            Container(
              height: 10,
              width: constraints.maxWidth * (score / 100),
              decoration: BoxDecoration(
                color: color,
                borderRadius: BorderRadius.circular(5),
              ),
            ),
          ],
        );
      },
    );
  }
}
