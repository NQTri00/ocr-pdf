from ocr_pdf.inspection import _classify_page


def test_digital_page():
    assert _classify_page(500, 0.0, 0)[0] == "digital"


def test_scanned_page():
    assert _classify_page(0, 0.95, 1)[0] == "scanned"


def test_mixed_page():
    assert _classify_page(500, 0.8, 1)[0] == "mixed"


def test_unknown_page():
    assert _classify_page(0, 0.0, 0)[0] == "unknown"
