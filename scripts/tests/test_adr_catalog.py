import pytest

from scripts.check_adr_catalog import (
    AdrRecord,
    load_adr_records,
    parse_adr,
    render_catalog,
    replace_catalog,
    run,
)

ADR_CONTENT = """# ADR 0001: Example decision

**Status:** Accepted
**Summary:** A concise decision summary.
"""
ADR_FILENAME = "0001-example-decision.md"
ADR_README = """# ADRs

<!-- ADR-CATALOG:START -->
<!-- ADR-CATALOG:END -->
"""


def test_parse_adr_extracts_metadata():
    record = parse_adr(ADR_FILENAME, ADR_CONTENT)

    assert record == AdrRecord(
        identifier="0001",
        filename=ADR_FILENAME,
        title="Example decision",
        status="Accepted",
        summary="A concise decision summary.",
    )


@pytest.mark.parametrize(
    ("field", "line"),
    [
        ("title", "# ADR 0001: "),
        ("status", "**Status:**"),
        ("summary", "**Summary:**"),
    ],
)
@pytest.mark.parametrize("value", ["", "   "])
def test_parse_adr_rejects_empty_or_whitespace_metadata(field, line, value):
    content = ADR_CONTENT.replace(
        {
            "title": "# ADR 0001: Example decision",
            "status": "**Status:** Accepted",
            "summary": "**Summary:** A concise decision summary.",
        }[field],
        line + value,
    )

    with pytest.raises(ValueError):
        parse_adr(ADR_FILENAME, content)


def test_parse_adr_trims_metadata_fields():
    content = (
        ADR_CONTENT.replace(
            "# ADR 0001: Example decision",
            "# ADR 0001:   Example decision  ",
        )
        .replace(
            "**Status:** Accepted",
            "**Status:**   Accepted  ",
        )
        .replace(
            "**Summary:** A concise decision summary.",
            "**Summary:**   A concise decision summary.  ",
        )
    )

    record = parse_adr(ADR_FILENAME, content)

    assert record.title == "Example decision"
    assert record.status == "Accepted"
    assert record.summary == "A concise decision summary."


def test_parse_adr_rejects_missing_metadata():
    with pytest.raises(ValueError):
        parse_adr(ADR_FILENAME, "# ADR 0001: Example decision\n\n**Status:** Accepted\n")


def test_parse_adr_rejects_filename_title_mismatch():
    with pytest.raises(ValueError) as error:
        parse_adr(ADR_FILENAME, ADR_CONTENT.replace("ADR 0001", "ADR 0002"))

    assert ADR_FILENAME in str(error.value)


def test_load_adr_records_sorts_records_by_filename(tmp_path):
    (tmp_path / "0002-second.md").write_text(
        ADR_CONTENT.replace("0001", "0002").replace("Example decision", "Second"),
        encoding="utf-8",
    )
    (tmp_path / ADR_FILENAME).write_text(ADR_CONTENT, encoding="utf-8")

    records = load_adr_records(tmp_path)

    assert [record.identifier for record in records] == ["0001", "0002"]


def test_load_adr_records_rejects_numbered_filename_without_four_digits(tmp_path):
    malformed_filename = "002-example-decision.md"
    (tmp_path / malformed_filename).write_text(
        ADR_CONTENT.replace("0001", "002"),
        encoding="utf-8",
    )

    with pytest.raises(ValueError, match=malformed_filename):
        load_adr_records(tmp_path)


def test_render_catalog_creates_linked_markdown_rows():
    catalog = render_catalog(
        [AdrRecord("0001", ADR_FILENAME, "Example decision", "Accepted", "Summary.")]
    )

    assert catalog.splitlines() == [
        "| ID | Title | Status | Summary |",
        "|---|---|---|---|",
        f"| [0001]({ADR_FILENAME}) | Example decision | Accepted | Summary. |",
    ]


def test_replace_catalog_preserves_content_outside_markers():
    updated = replace_catalog(ADR_README, "catalog rows")

    assert updated == """# ADRs

<!-- ADR-CATALOG:START -->
catalog rows
<!-- ADR-CATALOG:END -->
"""


def test_replace_catalog_rejects_missing_markers():
    with pytest.raises(ValueError):
        replace_catalog("# ADRs\n", "catalog rows")


def test_run_generates_index_from_adr_sources(tmp_path, capsys):
    adr_directory = tmp_path / "adr"
    adr_directory.mkdir()
    (adr_directory / ADR_FILENAME).write_text(ADR_CONTENT, encoding="utf-8")
    index_path = adr_directory / "README.md"
    index_path.write_text(ADR_README, encoding="utf-8")

    result = run(False, adr_directory, index_path)

    assert result == 0
    assert "Example decision" in index_path.read_text(encoding="utf-8")
    assert "Generated README.md." in capsys.readouterr().out


def test_run_check_reports_stale_index_without_writing(tmp_path, capsys):
    adr_directory = tmp_path / "adr"
    adr_directory.mkdir()
    (adr_directory / ADR_FILENAME).write_text(ADR_CONTENT, encoding="utf-8")
    index_path = adr_directory / "README.md"
    index_path.write_text(ADR_README, encoding="utf-8")

    result = run(True, adr_directory, index_path)

    assert result == 1
    assert index_path.read_text(encoding="utf-8") == ADR_README
    assert "out of date" in capsys.readouterr().out


def test_run_check_accepts_current_index(tmp_path, capsys):
    adr_directory = tmp_path / "adr"
    adr_directory.mkdir()
    (adr_directory / ADR_FILENAME).write_text(ADR_CONTENT, encoding="utf-8")
    index_path = adr_directory / "README.md"
    index_path.write_text(ADR_README, encoding="utf-8")
    assert run(False, adr_directory, index_path) == 0

    result = run(True, adr_directory, index_path)

    assert result == 0
    assert "up to date" in capsys.readouterr().out
