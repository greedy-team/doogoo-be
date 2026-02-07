---
name: "\U0001F41E Bug"
about: 버그를 발견했습니다/수정합니다.
title: "[BUG]"
labels: bug
assignees: ''

---

body:
  - type: textarea
    attributes:
      label: 🐞 버그 설명
      description: 어떤 버그인지 명확하게 작성해 주세요.
      placeholder: 무엇이, 언제, 어떻게 잘못 동작했는지
    validations:
      required: true

  - type: textarea
    attributes:
      label: 🔁 재현 방법
      description: 버그를 재현하는 과정을 단계별로 작성해 주세요.
      placeholder: |
        1. 
        2. 
        3.
    validations:
      required: true

  - type: textarea
    attributes:
      label: ✅ 기대 동작
      description: 정상적으로 동작했을 때의 결과를 작성해 주세요.
      placeholder: 원래 기대했던 동작
    validations:
      required: true

  - type: textarea
    attributes:
      label: 🖥 환경
      description: 버그가 발생한 환경을 작성해 주세요.
      placeholder: |
        - OS:
        - Browser:
        - Version:

  - type: textarea
    attributes:
      label: 🙋🏻 참고 자료
      description: 스크린샷, 로그, 관련 링크가 있다면 첨부해 주세요.
