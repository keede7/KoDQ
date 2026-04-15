import 'package:flutter/cupertino.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:kodq/main.dart';

void main() {
  testWidgets('App smoke test — KoDQApp renders without error', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const KoDQApp());
    expect(find.byType(CupertinoApp), findsOneWidget);
  });
}
