import request from '@/utils/request'

export function listAggregateSubjectTemplate(query) {
  return request({
    url: '/warehouse/aggregate/subjectTemplate/list',
    method: 'get',
    params: query
  })
}

export function getAggregateSubjectTemplate(templateId) {
  return request({
    url: '/warehouse/aggregate/subjectTemplate/' + templateId,
    method: 'get'
  })
}

export function getNextAggregateSubjectTemplateCode() {
  return request({
    url: '/warehouse/aggregate/subjectTemplate/nextCode',
    method: 'get'
  })
}

export function addAggregateSubjectTemplate(data) {
  return request({
    url: '/warehouse/aggregate/subjectTemplate',
    method: 'post',
    data: data
  })
}

export function updateAggregateSubjectTemplate(data) {
  return request({
    url: '/warehouse/aggregate/subjectTemplate',
    method: 'put',
    data: data
  })
}

export function activateAggregateSubjectTemplate(templateId) {
  return request({
    url: '/warehouse/aggregate/subjectTemplate/activate/' + templateId,
    method: 'put'
  })
}

export function delAggregateSubjectTemplate(templateId) {
  return request({
    url: '/warehouse/aggregate/subjectTemplate/' + templateId,
    method: 'delete'
  })
}

export function copyAggregateSubjectTemplate(templateId) {
  return request({
    url: '/warehouse/aggregate/subjectTemplate/copy/' + templateId,
    method: 'post'
  })
}

export function listEnabledAggregateSubjectTemplateOptions() {
  return request({
    url: '/warehouse/aggregate/subjectTemplate/options/enabled',
    method: 'get'
  })
}
