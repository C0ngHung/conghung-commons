export const meta = {
  name: 'review-changes',
  description: 'Review diff hiện tại theo nhiều dimension (correctness/security/simplicity) rồi verify từng finding bằng agent phản biện',
  phases: [
    { title: 'Review', detail: 'mỗi dimension 1 agent đọc diff' },
    { title: 'Verify', detail: 'agent phản biện xác nhận từng finding' },
  ],
}

// Cách chạy: nói với Claude "use a workflow: review-changes" (workflow cần opt-in rõ ràng).
// Mục đích minh hoạ trong guide: dùng fan-out đa agent cho phase Review — nhiều góc nhìn độc lập,
// rồi verify đối kháng để loại finding sai (chống hallucination).

const FINDINGS_SCHEMA = {
  type: 'object',
  properties: {
    findings: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          file: { type: 'string' },
          line: { type: 'integer' },
          severity: { type: 'string', enum: ['high', 'medium', 'low'] },
          summary: { type: 'string' },
          rationale: { type: 'string' },
        },
        required: ['file', 'summary'],
      },
    },
  },
  required: ['findings'],
}

const VERDICT_SCHEMA = {
  type: 'object',
  properties: {
    isReal: { type: 'boolean' },
    reason: { type: 'string' },
  },
  required: ['isReal', 'reason'],
}

const DIMENSIONS = [
  { key: 'correctness', prompt: 'Review diff (git diff) của repo hiện tại, TẬP TRUNG lỗi correctness: null, off-by-one, edge case, sai contract API, sai mapping exception→status. Với repo conghung-commons (Java 21, Spring Boot 4, Jackson 3). Trả về findings.' },
  { key: 'security', prompt: 'Review diff (git diff) hiện tại, TẬP TRUNG bảo mật: log-injection, lộ thông tin nhạy cảm trong message lỗi, deserialization không an toàn, nullness. Trả về findings.' },
  { key: 'simplicity', prompt: 'Review diff (git diff) hiện tại, TẬP TRUNG đơn giản hoá & tái sử dụng: code lặp, có thể dùng lại util/factory sẵn có (ApiResult.fail, ValidationError, sanitize), over-engineering. Trả về findings.' },
]

phase('Review')

const results = await pipeline(
  DIMENSIONS,
  (d) => agent(d.prompt, { label: `review:${d.key}`, phase: 'Review', schema: FINDINGS_SCHEMA }),
  (review, d) =>
    parallel(
      (review?.findings ?? []).map((f) => () =>
        agent(
          `Phản biện finding sau — nó có THẬT không? Mặc định hoài nghi. Đọc code liên quan trước khi kết luận.\nFile: ${f.file}${f.line ? ':' + f.line : ''}\nVấn đề: ${f.summary}\nLý lẽ: ${f.rationale ?? ''}`,
          { label: `verify:${d.key}:${f.file}`, phase: 'Verify', schema: VERDICT_SCHEMA }
        ).then((v) => ({ ...f, dimension: d.key, verdict: v }))
      )
    )
)

const confirmed = results
  .flat()
  .filter(Boolean)
  .filter((f) => f.verdict?.isReal)

log(`Confirmed ${confirmed.length} finding(s) sau khi phản biện`)

return { confirmed }
