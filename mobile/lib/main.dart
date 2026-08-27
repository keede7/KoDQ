import 'package:flutter/cupertino.dart';
import 'screens/home_screen.dart';

void main() {
  runApp(const KoDQApp());
}

class KoDQApp extends StatelessWidget {
  const KoDQApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const CupertinoApp(
      title: 'KoDQ',
      theme: CupertinoThemeData(
        primaryColor: CupertinoColors.activeBlue,
      ),
      debugShowCheckedModeBanner: false,
      home: HomeScreen(),
    );
  }
}
