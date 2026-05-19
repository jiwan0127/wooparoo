import csv
import time
import random
import os
from playwright.sync_api import sync_playwright, TimeoutError

URL = "https://wooparoo-odyssey.hangame.com/probability"
TARGET_PAGE = 23
CSV_FILE = "wooparoo_magic_cross_rate.csv"

TIME_BUTTON_XPATH = (
    "//tr[td[2]='30' and td[3]='20' "
    "and contains(td[4],'매직 크로스') and td[5]='X']"
    "/td[1]//button"
)

# =====================
# 유틸
# =====================
def sleep():
    t = random.uniform(0.8, 1.3)
    time.sleep(t)

def format_time(sec):
    m, s = divmod(int(sec), 60)
    h, m = divmod(m, 60)
    if h:
        return f"{h}시간 {m}분"
    if m:
        return f"{m}분 {s}초"
    return f"{s}초"

def collect_cross_table(page):
    return page.evaluate("""
    () => {
        const rows = document.querySelectorAll("tbody tr");
        return Array.from(rows).map(tr => {
            const cells = tr.querySelectorAll("th, td");
            return [
                cells[0]?.innerText.trim() ?? "",
                cells[1]?.innerText.trim() ?? "",
                cells[2]?.innerText.replace('%','').trim() ?? ""
            ];
        });
    }
    """)

# =====================
# 메인
# =====================
with sync_playwright() as p:
    browser = p.chromium.launch(headless=False)
    context = browser.new_context()
    page = context.new_page()

    # =====================
    # CSV 재개 처리
    # =====================
    processed = set()
    file_exists = os.path.exists(CSV_FILE)

    if file_exists:
        with open(CSV_FILE, encoding="utf-8-sig") as f:
            reader = csv.reader(f)
            next(reader, None)
            for r in reader:
                processed.add(r[0])

    csv_file = open(CSV_FILE, "a", newline="", encoding="utf-8-sig")
    writer = csv.writer(csv_file)
    if not file_exists:
        writer.writerow(["wooparoo", "left", "right", "rate"])

    print(f"📄 이미 수집된 우파루: {len(processed)}")

    # =====================
    # 초기 진입 (1회)
    # =====================
    page.goto(URL, timeout=60000)

    while True:
        cur = int(page.locator('button[aria-current="true"]').inner_text())
        if cur >= TARGET_PAGE:
            break
        page.locator('button[aria-label="Go to next page"]').click()
        page.wait_for_selector(
            f'button[aria-current="true"]:has-text("{cur + 1}")',
            timeout=5000
        )

    print("✅ 이벤트 페이지 도달")

    # 시간 클릭
    page.locator(TIME_BUTTON_XPATH).click()
    page.wait_for_selector(
        "table:has(th:has-text('우파루 이름')) tbody tr",
        timeout=30000
    )

    # =====================
    # 우파루 이름 목록 고정 (문자열)
    # =====================
    names = page.locator(
        "table:has(th:has-text('우파루 이름')) tbody tr td:first-child button"
    ).all_inner_texts()

    names = list(dict.fromkeys(names))

    total = len(names)
    completed = len(processed)
    start_time = time.time()

    print(f"🐾 총 우파루 수: {total}")

    failed = []

    # =====================
    # 우파루 루프
    # =====================
    for name in names:
        if name in processed:
            continue

        print(f"\n▶ {name} 시작")

        try:
            # ⚠️ strict mode 회피 핵심
            btn = page.evaluate_handle(
                """(targetName) => {
                    // 1️⃣ 모든 table 탐색
                    const tables = document.querySelectorAll("table");

                    let targetTable = null;

                    for (const table of tables) {
                        const ths = table.querySelectorAll("th");
                        for (const th of ths) {
                            if (th.innerText.trim() === "우파루 이름") {
                                targetTable = table;
                                break;
                            }
                        }
                        if (targetTable) break;
                    }

                    if (!targetTable) return null;

                    // 2️⃣ tbody tr 순회
                    const rows = targetTable.querySelectorAll("tbody tr");

                    for (const tr of rows) {
                        const btn = tr.querySelector("td:first-child button");
                        if (!btn) continue;

                        if (btn.innerText.trim() === targetName) {
                            btn.scrollIntoView({ block: "center" });
                            return btn;
                        }
                    }

                    return null;
                }""",
                name
            )

            if not btn:
                raise Exception(f"우파루 버튼 클릭 실패: {name}")

            btn.as_element().click()

            page.wait_for_selector(
                "th:has-text('왼쪽 우파루')",
                timeout=30000
            )

            rows = collect_cross_table(page)
            for left, right, rate in rows:
                writer.writerow([name, left, right, rate])

            processed.add(name)
            completed += 1

            elapsed = time.time() - start_time
            avg = elapsed / completed
            eta = avg * (total - completed)

            print(
                f"✅ 완료 | {completed}/{total} "
                f"({completed/total*100:.2f}%) | "
                f"⏱ {format_time(elapsed)} | "
                f"⏳ ETA {format_time(eta)}"
            )

            # 목록 복귀 (SPA 내부)
            page.evaluate("window.history.back()")
            page.wait_for_selector(
                "table:has(th:has-text('우파루 이름')) tbody tr",
                timeout=30000
            )

        except Exception as e:
            print(f"❌ 실패: {name} ({e})")
            failed.append(name)

        sleep()

    csv_file.close()

    print("\n🎉 수집 완료")
    print(f"❌ 실패 목록 ({len(failed)}): {failed}")

    input("엔터 누르면 종료")
    browser.close()
