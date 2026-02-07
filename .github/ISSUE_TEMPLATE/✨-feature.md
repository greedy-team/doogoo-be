---
name: "✨ Feature"
about: 새로운 기능을 추가합니다.
title: '["feature"]'
labels: enhancement
assignees: ''

---

body:
  - type: textarea
    attributes:
      label: 📄 설명
      description: 새로운 기능에 대한 설명을 작성해 주세요.
      placeholder: 자세히 적을수록 좋습니다!
    validations:
      required: true

  - type: textarea
    attributes:
      label: ✅ 발생한 문제
      description: 문제가 발생한 상황을 작성해주세요.
      placeholder: 최대한 세분화 해서 적어주세요!
    validations:
      required: true

  - type: textarea
    attributes:
      label: 🙋🏻 참고 자료
      description: 참고 자료가 있다면 작성해 주세요.
