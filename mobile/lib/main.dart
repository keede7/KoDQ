import 'package:flutter/material.dart';
import 'screens/home_screen.dart';

void main() {
  runApp(const KoDQApp());
}

class KoDQApp extends StatelessWidget {
  const KoDQApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'KoDQ',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const HomeScreen(),
    );
  }
}
